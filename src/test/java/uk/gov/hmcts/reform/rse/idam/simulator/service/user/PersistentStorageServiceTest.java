package uk.gov.hmcts.reform.rse.idam.simulator.service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage"})
public class PersistentStorageServiceTest {

    private static final String USER_ID = "user-id";
    private static final String EMAIL = "user@example.com";

    @TempDir
    Path tempDir;

    @Test
    public void shouldKeepCommittedUsersWhenTheStoreIsNeverClosed() {
        Path store = tempDir.resolve("users.db");

        new PersistentStorageService(store.toString()).putSimObject(USER_ID, user(USER_ID, EMAIL));

        assertThat(new PersistentStorageService(store.toString()).getByEmail(EMAIL)).isPresent();
    }

    @Test
    public void shouldKeepAChangeMadeThroughPutSimObject() {
        Path store = tempDir.resolve("users.db");
        PersistentStorageService service = new PersistentStorageService(store.toString());
        service.putSimObject(USER_ID, user(USER_ID, EMAIL));

        SimObject stored = service.getByUserId(USER_ID);
        stored.setMostRecentCode("a-code");
        service.putSimObject(USER_ID, stored);

        assertThat(new PersistentStorageService(store.toString()).getByCode("a-code")).isPresent();
    }

    @Test
    public void shouldCreateMissingParentDirectories() {
        Path store = tempDir.resolve("missing").resolve("parents").resolve("users.db");

        new PersistentStorageService(store.toString()).putSimObject(USER_ID, user(USER_ID, EMAIL));

        assertThat(store).exists();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldOpenAStoreWrittenWithoutTransactions() {
        Path store = tempDir.resolve("users.db");
        DB legacy = DBMaker.fileDB(store.toFile()).fileLockDisable().checksumHeaderBypass().make();
        Map<String, SimObject> memories = legacy.hashMap("memories")
            .keySerializer(Serializer.STRING)
            .valueSerializer(Serializer.JAVA)
            .createOrOpen();
        memories.put(USER_ID, user(USER_ID, EMAIL));
        legacy.commit();
        legacy.close();

        assertThat(new PersistentStorageService(store.toString()).getByEmail(EMAIL)).isPresent();
    }

    @Test
    public void shouldStartEmptyAndMoveACorruptStoreAside() throws IOException {
        Path store = tempDir.resolve("users.db");
        byte[] corrupt = "this is not a store".repeat(100).getBytes();
        Files.write(store, corrupt);

        PersistentStorageService service = new PersistentStorageService(store.toString());

        assertThat(service.getAll()).isEmpty();
        assertThat(tempDir.resolve("users.db.corrupt")).hasBinaryContent(corrupt);
        service.putSimObject(USER_ID, user(USER_ID, EMAIL));
        assertThat(new PersistentStorageService(store.toString()).getByEmail(EMAIL)).isPresent();
    }

    @Test
    public void shouldStartEmptyFromAnEmptyStoreFile() throws IOException {
        Path store = tempDir.resolve("users.db");
        Files.createFile(store);

        assertThat(new PersistentStorageService(store.toString()).getAll()).isEmpty();
    }

    @Test
    public void shouldMoveTheWriteAheadLogAsideWithACorruptStore() throws IOException {
        Path store = tempDir.resolve("users.db");
        Path wal = tempDir.resolve("users.db.wal.0");
        Files.write(store, "this is not a store".repeat(100).getBytes());
        Files.write(wal, "stale write ahead log".getBytes());

        new PersistentStorageService(store.toString());

        assertThat(tempDir.resolve("users.db.corrupt.wal.0")).hasContent("stale write ahead log");
    }

    @Test
    public void shouldKeepUsersInMemoryWhenTheStorePathCannotBeUsed() throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("users.db"));
        Path keptFile = Files.createFile(directory.resolve("kept"));

        PersistentStorageService service = new PersistentStorageService(directory.toString());
        service.putSimObject(USER_ID, user(USER_ID, EMAIL));

        assertThat(service.getByEmail(EMAIL)).isPresent();
        assertThat(keptFile).exists();
    }

    @Test
    public void shouldKeepEveryUserWrittenConcurrently() throws Exception {
        Path store = tempDir.resolve("users.db");
        PersistentStorageService service = new PersistentStorageService(store.toString());
        int threads = 8;
        int usersPerThread = 25;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<?>> writers = new ArrayList<>();
        for (int thread = 0; thread < threads; thread++) {
            String prefix = "thread-" + thread + "-";
            writers.add(executor.submit(() -> {
                for (int number = 0; number < usersPerThread; number++) {
                    String id = prefix + number;
                    service.putSimObject(id, user(id, id + "@example.com"));
                    assertThat(service.getByEmail(id + "@example.com")).isPresent();
                }
            }));
        }
        for (Future<?> writer : writers) {
            writer.get();
        }
        executor.shutdown();

        assertThat(new PersistentStorageService(store.toString()).getAll()).hasSize(threads * usersPerThread);
    }

    private SimObject user(String id, String email) {
        return SimObject.builder()
            .id(id)
            .email(email)
            .forename("Forename")
            .surname("Surname")
            .roles(new ArrayList<>(List.of("caseworker")))
            .build();
    }
}
