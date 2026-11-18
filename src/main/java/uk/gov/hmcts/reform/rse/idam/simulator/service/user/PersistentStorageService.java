package uk.gov.hmcts.reform.rse.idam.simulator.service.user;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@ConditionalOnProperty(name = "simulator.storage.type", havingValue = "persistent")
public class PersistentStorageService extends LiveMemoryService implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(PersistentStorageService.class);

    private static final String CORRUPT_SUFFIX = ".corrupt";
    private static final String WAL_INFIX = ".wal.";
    private static final String STARTUP_CHECK_KEY = "persistent-storage-startup-check";

    private final DB db;

    public PersistentStorageService(@Value("${simulator.storage.persistent-file}") String dbFile) {
        this.db = openStore(Path.of(dbFile).toAbsolutePath());
        super.memories = memoriesOf(db);
    }

    @Override
    public synchronized void putSimObject(String userId, SimObject simObject) {
        super.putSimObject(userId, simObject);
        db.commit();
    }

    private static DB openStore(Path storeFile) {
        try {
            return openCheckedStore(storeFile);
        } catch (IOException | RuntimeException | Error exception) {
            LOG.warn("Persistent user store {} is unusable ({}), moving it aside and starting from an empty store",
                     storeFile, exception.getClass().getSimpleName());
        }
        try {
            moveAside(storeFile);
            return openCheckedStore(storeFile);
        } catch (IOException | RuntimeException | Error exception) {
            LOG.warn("Persistent user store {} cannot be replaced ({}), users will only be kept in memory",
                     storeFile, exception.getClass().getSimpleName());
            return DBMaker.memoryDB().transactionEnable().make();
        }
    }

    private static DB openCheckedStore(Path storeFile) throws IOException {
        Files.createDirectories(storeFile.getParent());
        DB store = DBMaker.fileDB(storeFile.toFile())
            .fileLockDisable()
            .checksumHeaderBypass()
            .transactionEnable()
            .make();
        Map<String, SimObject> storedUsers = memoriesOf(store);
        storedUsers.values().forEach(SimObject::getId);
        storedUsers.put(STARTUP_CHECK_KEY, SimObject.builder().build());
        storedUsers.remove(STARTUP_CHECK_KEY);
        store.commit();
        return store;
    }

    private static void moveAside(Path storeFile) throws IOException {
        Path directory = storeFile.getParent();
        String storeName = storeFile.getFileName().toString();
        String walPrefix = storeName + WAL_INFIX;
        List<Path> walFiles;
        try (Stream<Path> files = Files.list(directory)) {
            walFiles = files
                .filter(file -> file.getFileName().toString().startsWith(walPrefix))
                .collect(Collectors.toList());
        }
        for (Path walFile : walFiles) {
            String walNumber = walFile.getFileName().toString().substring(walPrefix.length());
            Path target = directory.resolve(storeName + CORRUPT_SUFFIX + WAL_INFIX + walNumber);
            Files.move(walFile, target, StandardCopyOption.REPLACE_EXISTING);
        }
        if (Files.isRegularFile(storeFile)) {
            Path target = directory.resolve(storeName + CORRUPT_SUFFIX);
            Files.move(storeFile, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, SimObject> memoriesOf(DB store) {
        return store.hashMap("memories")
            .keySerializer(Serializer.STRING)
            .valueSerializer(Serializer.JAVA)
            .createOrOpen();
    }
}
