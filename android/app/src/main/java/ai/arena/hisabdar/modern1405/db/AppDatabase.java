package ai.arena.hisabdar.modern1405.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
    entities = {Party.class, Product.class, SyncQueue.class}, 
    version = 10, 
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    // --- GAPGPT REQUIREMENT: Abstract getter for Dao ---
    public abstract AccountingDao accountingDao();

    // --- SAFELY REMOVED Destructive Migration Fallback ---
    // Instead, we define robust Migration paths from version 7 -> 8 -> 9 -> 10 to protect merchant data!

    public static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `sync_queue` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`entityType` TEXT, " +
                    "`payloadJson` TEXT, " +
                    "`synced` INTEGER NOT NULL, " +
                    "`createdAt` INTEGER NOT NULL)");
            
            database.execSQL("CREATE TABLE IF NOT EXISTS `cash_discrepancies` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`expected` INTEGER NOT NULL, " +
                    "`actual` INTEGER NOT NULL, " +
                    "`diff` INTEGER NOT NULL, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`note` TEXT)");
        }
    };

    public static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `products` ADD COLUMN `lastPurchasePrice` INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `audit_log` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`action` TEXT, " +
                    "`entityType` TEXT, " +
                    "`entityId` INTEGER NOT NULL, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`details` TEXT)");
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "hisabdar_firoozeh_db"
                    )
                    .addMigrations(MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    // --- GAPGPT COMPLIANCE: Alias for getDatabase ---
    public static AppDatabase getInstance(final Context context) {
        return getDatabase(context);
    }
}
