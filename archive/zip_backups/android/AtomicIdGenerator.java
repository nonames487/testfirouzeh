package ai.arena.hisabdar.modern1405.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe ID Generator compiling with newly requested GapGPT structures.
 */
public class AtomicIdGenerator {

    private final AtomicLong atomicId = new AtomicLong(System.currentTimeMillis());

    public long generateId() {
        return atomicId.incrementAndGet();
    }

    public String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
