package uk.gov.hmcts.reform.rse.idam.simulator.service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage"})
public class PersistentStorageServiceTest {

    private static final String USER_ID = "user-id";
    private static final String EMAIL = "user@example.com";
    private static final String CORRUPT_CONTENT = "this is not a store";
    private static final String READY = "ready";
    private static final String WRITE = "write";

    @TempDir
    Path tempDir;

    @Test
    public void shouldKeepCommittedUsersWhenTheProcessIsKilled() throws Exception {
        Path store = tempDir.resolve("users.db");

        try (StoreProcess process = StoreProcess.start(store)) {
            process.writeUser();
            process.kill();
        }

        try (PersistentStorageService reopened = new PersistentStorageService(store.toString())) {
            assertThat(reopened.getByEmail(EMAIL)).isPresent();
        }
        assertThat(backups()).isEmpty();
    }

    @Test
    public void shouldKeepAChangeMadeThroughPutSimObject() {
        Path store = tempDir.resolve("users.db");
        try (PersistentStorageService service = new PersistentStorageService(store.toString())) {
            service.putSimObject(USER_ID, user(USER_ID, EMAIL));

            SimObject stored = service.getByUserId(USER_ID);
            stored.setMostRecentCode("a-code");
            service.putSimObject(USER_ID, stored);
        }

        try (PersistentStorageService reopened = new PersistentStorageService(store.toString())) {
            assertThat(reopened.getByCode("a-code")).isPresent();
        }
    }

    @Test
    public void shouldCreateMissingParentDirectories() {
        Path store = tempDir.resolve("missing").resolve("parents").resolve("users.db");

        try (PersistentStorageService service = new PersistentStorageService(store.toString())) {
            service.putSimObject(USER_ID, user(USER_ID, EMAIL));
        }

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

        try (PersistentStorageService service = new PersistentStorageService(store.toString())) {
            assertThat(service.getByEmail(EMAIL)).isPresent();
        }
    }

    @Test
    public void shouldStartEmptyAndMoveACorruptStoreAside() throws IOException {
        Path store = tempDir.resolve("users.db");
        byte[] corrupt = CORRUPT_CONTENT.repeat(100).getBytes(StandardCharsets.UTF_8);
        Files.write(store, corrupt);

        try (PersistentStorageService service = new PersistentStorageService(store.toString())) {
            assertThat(service.getAll()).isEmpty();
            service.putSimObject(USER_ID, user(USER_ID, EMAIL));
        }

        assertThat(backups()).singleElement().satisfies(backup -> assertThat(backup).hasBinaryContent(corrupt));
        try (PersistentStorageService reopened = new PersistentStorageService(store.toString())) {
            assertThat(reopened.getByEmail(EMAIL)).isPresent();
        }
    }

    @Test
    public void shouldKeepEveryBackupWhenTheStoreIsCorruptedAgain() throws IOException {
        Path store = tempDir.resolve("users.db");
        final Path earlierBackup = Files.writeString(tempDir.resolve("users.db.corrupt"), "an earlier backup");
        byte[] first = (CORRUPT_CONTENT + " first").repeat(100).getBytes(StandardCharsets.UTF_8);
        byte[] second = (CORRUPT_CONTENT + " second").repeat(100).getBytes(StandardCharsets.UTF_8);

        Files.write(store, first);
        new PersistentStorageService(store.toString()).close();
        Files.write(store, second);
        new PersistentStorageService(store.toString()).close();

        assertThat(earlierBackup).hasContent("an earlier backup");
        List<byte[]> backupContents = new ArrayList<>();
        for (Path backup : backups()) {
            backupContents.add(Files.readAllBytes(backup));
        }
        assertThat(backupContents).containsExactlyInAnyOrder(first, second);
    }

    @Test
    public void shouldStartEmptyFromAnEmptyStoreFile() throws IOException {
        Path store = tempDir.resolve("users.db");
        Files.createFile(store);

        try (PersistentStorageService service = new PersistentStorageService(store.toString())) {
            assertThat(service.getAll()).isEmpty();
        }
    }

    @Test
    public void shouldMoveTheWriteAheadLogAsideWithACorruptStore() throws IOException {
        Path store = tempDir.resolve("users.db");
        Files.write(store, CORRUPT_CONTENT.repeat(100).getBytes(StandardCharsets.UTF_8));
        Files.writeString(tempDir.resolve("users.db.wal.0"), "stale write ahead log");

        new PersistentStorageService(store.toString()).close();

        Path backup = backups().get(0);
        assertThat(tempDir.resolve(backup.getFileName() + ".wal.0")).hasContent("stale write ahead log");
    }

    @Test
    public void shouldMoveAsideAStoreWithARecordThatCannotBeDeserialized() throws IOException {
        Path store = tempDir.resolve("users.db");
        writeRecord(store, new UnreadableRecord());

        try (PersistentStorageService service = new PersistentStorageService(store.toString())) {
            assertThat(service.getAll()).isEmpty();
        }

        assertThat(backups()).hasSize(1);
    }

    @Test
    public void shouldMoveAsideAStoreWithARecordOfTheWrongType() throws IOException {
        Path store = tempDir.resolve("users.db");
        writeRecord(store, "not a user");

        try (PersistentStorageService service = new PersistentStorageService(store.toString())) {
            assertThat(service.getAll()).isEmpty();
        }

        assertThat(backups()).hasSize(1);
    }

    @Test
    public void shouldStopWithoutTouchingTheStoreWhenReadingItRunsOutOfMemory() throws IOException {
        Path store = tempDir.resolve("users.db");
        writeRecord(store, new OutOfMemoryOnRead());
        Map<String, String> before = contentsOfTempDir();

        assertThatThrownBy(() -> new PersistentStorageService(store.toString()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(store.toString())
            .hasCauseInstanceOf(OutOfMemoryError.class);

        assertThat(contentsOfTempDir()).isEqualTo(before);
    }

    @Test
    public void shouldStopWhenTheStorePathIsADirectory() throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("users.db"));
        Path keptFile = Files.createFile(directory.resolve("kept"));

        assertThatThrownBy(() -> new PersistentStorageService(directory.toString()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(directory.toString());

        assertThat(keptFile).exists();
        assertThat(backups()).isEmpty();
    }

    @Test
    public void shouldStopWithoutTouchingAReadOnlyStore() throws IOException {
        Path store = tempDir.resolve("users.db");
        new PersistentStorageService(store.toString()).close();
        Files.setPosixFilePermissions(store, PosixFilePermissions.fromString("r--r--r--"));
        assumeFalse(Files.isWritable(store));
        Map<String, String> before = contentsOfTempDir();

        assertThatThrownBy(() -> new PersistentStorageService(store.toString()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(store.toString());

        assertThat(contentsOfTempDir()).isEqualTo(before);
    }

    @Test
    public void shouldStopWithoutTouchingAStoreLockedByAnotherProcess() throws Exception {
        Path store = tempDir.resolve("users.db");

        try (StoreProcess process = StoreProcess.start(store)) {
            Map<String, String> before = contentsOfTempDir();

            assertThatThrownBy(() -> new PersistentStorageService(store.toString()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(store.toString())
                .hasMessageContaining("locked by another process");

            assertThat(contentsOfTempDir()).isEqualTo(before);
            process.writeUser();
            process.kill();
        }

        try (PersistentStorageService reopened = new PersistentStorageService(store.toString())) {
            assertThat(reopened.getByEmail(EMAIL)).isPresent();
        }
        assertThat(backups()).isEmpty();
    }

    @Test
    public void shouldKeepEveryUserWrittenConcurrently() throws Exception {
        Path store = tempDir.resolve("users.db");
        int threads = 8;
        int usersPerThread = 25;
        try (PersistentStorageService service = new PersistentStorageService(store.toString())) {
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
        }

        try (PersistentStorageService reopened = new PersistentStorageService(store.toString())) {
            assertThat(reopened.getAll()).hasSize(threads * usersPerThread);
        }
    }

    private List<Path> backups() throws IOException {
        try (Stream<Path> files = Files.list(tempDir)) {
            return files
                .filter(file -> file.getFileName().toString().matches("users\\.db\\.corrupt-[0-9a-f-]+"))
                .collect(Collectors.toList());
        }
    }

    private Map<String, String> contentsOfTempDir() throws IOException {
        Map<String, String> contents = new TreeMap<>();
        try (Stream<Path> files = Files.list(tempDir)) {
            for (Path file : files.collect(Collectors.toList())) {
                contents.put(file.getFileName().toString(), new String(Files.readAllBytes(file),
                                                                       StandardCharsets.ISO_8859_1));
            }
        }
        return contents;
    }

    @SuppressWarnings("unchecked")
    private static void writeRecord(Path store, Object value) {
        DB db = DBMaker.fileDB(store.toFile()).checksumHeaderBypass().transactionEnable().make();
        Map<String, Object> memories = db.hashMap("memories")
            .keySerializer(Serializer.STRING)
            .valueSerializer(Serializer.JAVA)
            .createOrOpen();
        memories.put(USER_ID, value);
        db.commit();
        db.close();
    }

    private static SimObject user(String id, String email) {
        return SimObject.builder()
            .id(id)
            .email(email)
            .forename("Forename")
            .surname("Surname")
            .roles(new ArrayList<>(List.of("caseworker")))
            .build();
    }

    private static final class UnreadableRecord implements Serializable {

        private static final long serialVersionUID = 1L;

        private void readObject(ObjectInputStream in) throws InvalidObjectException {
            throw new InvalidObjectException("simulated");
        }
    }

    private static final class OutOfMemoryOnRead implements Serializable {

        private static final long serialVersionUID = 1L;

        private void readObject(ObjectInputStream in) {
            throw new OutOfMemoryError("simulated");
        }
    }

    public static final class StoreHolder {

        private StoreHolder() {
        }

        public static void main(String[] args) throws IOException {
            PersistentStorageService service = new PersistentStorageService(args[0]);
            PrintStream out = System.out;
            out.println(READY);
            out.flush();
            BufferedReader commands = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            String command = commands.readLine();
            while (command != null) {
                service.putSimObject(USER_ID, user(USER_ID, EMAIL));
                out.println(WRITE);
                out.flush();
                command = commands.readLine();
            }
        }
    }

    private static final class StoreProcess implements AutoCloseable {

        private final Process process;
        private final BufferedReader output;

        private StoreProcess(Process process) {
            this.process = process;
            this.output = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        }

        static StoreProcess start(Path store) throws IOException {
            Process process = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("java.class.path"),
                StoreHolder.class.getName(),
                store.toString()
            ).redirectError(ProcessBuilder.Redirect.DISCARD).start();
            StoreProcess storeProcess = new StoreProcess(process);
            storeProcess.awaitLine(READY);
            return storeProcess;
        }

        void writeUser() throws IOException {
            process.getOutputStream().write((WRITE + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().flush();
            awaitLine(WRITE);
        }

        void kill() throws InterruptedException {
            process.destroyForcibly();
            assertThat(process.waitFor(30, TimeUnit.SECONDS)).isTrue();
        }

        private void awaitLine(String expected) throws IOException {
            String line = output.readLine();
            while (line != null && !line.equals(expected)) {
                line = output.readLine();
            }
            assertThat(line).isEqualTo(expected);
        }

        @Override
        public void close() {
            process.destroyForcibly();
        }
    }
}
