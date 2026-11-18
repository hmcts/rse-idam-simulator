package uk.gov.hmcts.reform.rse.idam.simulator.service.user;

import org.mapdb.DB;
import org.mapdb.DBException;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@ConditionalOnProperty(name = "simulator.storage.type", havingValue = "persistent")
public class PersistentStorageService extends LiveMemoryService implements UserService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(PersistentStorageService.class);

    private static final String CORRUPT_INFIX = ".corrupt-";
    private static final String WAL_INFIX = ".wal.";
    private static final String STARTUP_CHECK_KEY = "persistent-storage-startup-check";
    private static final List<Class<? extends Exception>> CORRUPTION_EXCEPTIONS = List.of(
        DBException.DataCorruption.class,
        DBException.WrongFormat.class,
        DBException.NewMapDBFormat.class,
        DBException.WrongConfiguration.class,
        DBException.SerializationError.class,
        DBException.GetVoid.class,
        DBException.VolumeEOF.class,
        IndexOutOfBoundsException.class,
        NegativeArraySizeException.class,
        NullPointerException.class,
        ClassCastException.class,
        IllegalStateException.class
    );

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

    @Override
    public synchronized void close() {
        if (!db.isClosed()) {
            db.close();
        }
    }

    private static DB openStore(Path storeFile) {
        try {
            return openCheckedStore(storeFile);
        } catch (IOException | RuntimeException | Error exception) {
            if (!isCorruption(exception)) {
                throw storeUnusable(storeFile, exception);
            }
            LOG.warn("Persistent user store {} is corrupt ({}), moving it aside and starting from an empty store",
                     storeFile, exception.getClass().getSimpleName());
        }
        try {
            moveAside(storeFile);
            return openCheckedStore(storeFile);
        } catch (IOException | RuntimeException | Error exception) {
            throw storeUnusable(storeFile, exception);
        }
    }

    private static DB openCheckedStore(Path storeFile) throws IOException {
        Files.createDirectories(storeFile.getParent());
        DB store = DBMaker.fileDB(storeFile.toFile())
            .checksumHeaderBypass()
            .transactionEnable()
            .make();
        try {
            Map<String, SimObject> storedUsers = memoriesOf(store);
            storedUsers.values().forEach(SimObject::getId);
            storedUsers.put(STARTUP_CHECK_KEY, SimObject.builder().build());
            storedUsers.remove(STARTUP_CHECK_KEY);
            store.commit();
            return store;
        } catch (RuntimeException | Error exception) {
            closeAfterFailure(store, exception);
            throw exception;
        }
    }

    private static void closeAfterFailure(DB store, Throwable failure) {
        try {
            store.close();
        } catch (RuntimeException | Error closeFailure) {
            failure.addSuppressed(closeFailure);
        }
    }

    private static boolean isCorruption(Throwable exception) {
        return CORRUPTION_EXCEPTIONS.stream().anyMatch(type -> type.isInstance(exception))
            || exception instanceof DBException.VolumeIOError && exception.getCause() instanceof EOFException;
    }

    private static IllegalStateException storeUnusable(Path storeFile, Throwable cause) {
        if (cause instanceof DBException.FileLocked) {
            return new IllegalStateException(
                "Persistent user store " + storeFile + " is locked by another process. Stop that process or set"
                    + " simulator.storage.persistent-file to a different file. The store has not been changed.",
                cause
            );
        }
        return new IllegalStateException(
            "Persistent user store " + storeFile + " cannot be opened (" + cause.getClass().getSimpleName()
                + "). Fix the problem and restart. The store has not been moved or replaced.",
            cause
        );
    }

    private static void moveAside(Path storeFile) throws IOException {
        try (FileChannel channel = FileChannel.open(storeFile, StandardOpenOption.WRITE);
             FileLock lock = lockForMove(storeFile, channel)) {
            moveStoreFiles(storeFile);
        } catch (NoSuchFileException exception) {
            LOG.warn("Persistent user store {} has already been moved aside", storeFile);
        }
    }

    private static FileLock lockForMove(Path storeFile, FileChannel channel) throws IOException {
        try {
            FileLock lock = channel.tryLock();
            if (lock == null) {
                throw new DBException.FileLocked(storeFile, null);
            }
            return lock;
        } catch (OverlappingFileLockException exception) {
            return null;
        }
    }

    private static void moveStoreFiles(Path storeFile) throws IOException {
        Path directory = storeFile.getParent();
        String storeName = storeFile.getFileName().toString();
        String walPrefix = storeName + WAL_INFIX;
        String backupName = storeName + CORRUPT_INFIX + UUID.randomUUID();
        List<Path> walFiles;
        try (Stream<Path> files = Files.list(directory)) {
            walFiles = files
                .filter(file -> file.getFileName().toString().startsWith(walPrefix))
                .collect(Collectors.toList());
        }
        for (Path walFile : walFiles) {
            String walNumber = walFile.getFileName().toString().substring(walPrefix.length());
            Files.move(walFile, directory.resolve(backupName + WAL_INFIX + walNumber));
        }
        Path backup = directory.resolve(backupName);
        Files.move(storeFile, backup);
        LOG.warn("Persistent user store {} was moved aside to {}", storeFile, backup);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, SimObject> memoriesOf(DB store) {
        return store.hashMap("memories")
            .keySerializer(Serializer.STRING)
            .valueSerializer(Serializer.JAVA)
            .createOrOpen();
    }
}
