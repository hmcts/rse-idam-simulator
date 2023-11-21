package uk.gov.hmcts.reform.rse.idam.simulator.service.user;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "simulator.storage.type", havingValue = "persistent")
public class PersistentStorageService extends LiveMemoryService implements UserService {

    private final DB db;

    @SuppressWarnings("unchecked")
    public PersistentStorageService(@Value("${simulator.storage.persistent-file}") String dbFile) {
        this.db = DBMaker.fileDB(dbFile)
            .fileLockDisable()
            .checksumHeaderBypass()
            .make();
        super.memories = db.hashMap("memories")
            .keySerializer(Serializer.STRING)
            .valueSerializer(Serializer.JAVA)
            .createOrOpen();
    }

    @Override
    public void putSimObject(String userId, SimObject simObject) {
        super.putSimObject(userId, simObject);
        db.commit();
    }
}
