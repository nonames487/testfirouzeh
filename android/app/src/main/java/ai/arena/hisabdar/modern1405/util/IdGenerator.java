package ai.arena.hisabdar.modern1405.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe ID Generator fixing BUG-03.
 * Prevents key collision in high-concurrency offline sync logs by using atomic sequence starting values
 * and cryptographically secure UUID tokens as fallback identifiers.
 */
public class IdGenerator {

    private static final AtomicLong atomicId = new AtomicLong(System.currentTimeMillis());

    /**
     * Generates a unique, strictly-increasing positive long ID.
     * Guaranteed thread-safe and collision-free across local sessions.
     */
    public static long generateUniqueId() {
        return atomicId.incrementAndGet();
    }

    /**
     * Generates a global unique string ID.
     * Best suited for sync payloads and distributed databases (PostgreSQL backend synchronization).
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
