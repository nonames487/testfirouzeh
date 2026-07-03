package ai.arena.hisabdar.modern1405.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity for offline action logging and queueing.
 * Feeds WorkManager to guarantee eventual sync consistency.
 */
@Entity(tableName = "sync_queue")
public class SyncQueue {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String entityType; // e.g., "document", "party", "product"
    private String payloadJson; // Serialized JSON payload representing the change
    private boolean synced;
    private long createdAt;

    public SyncQueue(String entityType, String payloadJson, boolean synced, long createdAt) {
        this.entityType = entityType;
        this.payloadJson = payloadJson;
        this.synced = synced;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
