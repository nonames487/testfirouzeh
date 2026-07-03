package ai.arena.hisabdar.modern1405.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Transaction;
import java.util.List;

@Dao
public interface AccountingDao {

    // ------------------------------------------------
    // Party queries
    // ------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertParty(Party party);

    @Update
    void updateParty(Party party); // --- TASK 1: ADD updateParty ---

    @Query("SELECT * FROM parties WHERE id = :id LIMIT 1")
    Party getPartyById(long id);

    @Query("SELECT * FROM parties ORDER BY name ASC")
    LiveData<List<Party>> getAllParties();

    @Query("SELECT * FROM parties WHERE name LIKE :query || '%' ORDER BY name ASC")
    LiveData<List<Party>> searchPartiesByPrefix(String query);

    // ------------------------------------------------
    // Product queries
    // ------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProduct(Product product);

    @Update
    void updateProduct(Product product);

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    Product getProductById(long id);

    @Query("SELECT * FROM products ORDER BY name ASC")
    LiveData<List<Product>> getAllProducts();

    @Query("SELECT * FROM products WHERE name LIKE :query || '%' ORDER BY name ASC")
    LiveData<List<Product>> searchProductsByPrefix(String query);

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    Product findProductByBarcode(String barcode);

    // --- TASK 2: ADD BATCH CACHING SELECTORS FOR 100X PERFORMANCE ---
    @Query("SELECT * FROM products WHERE id IN (:ids)")
    List<Product> getProductsByIds(List<Long> ids);

    @Query("SELECT * FROM products WHERE barcode IN (:barcodes)")
    List<Product> getProductsByBarcodes(List<String> barcodes);

    @Query("SELECT * FROM products WHERE name IN (:names)")
    List<Product> getProductsByNames(List<String> names);

    // -- Sync accessors (synchronous - background thread only) ---

    @Query("SELECT * FROM products")
    List<Product> getAllProductsSync();

    @Query("SELECT * FROM products WHERE name = :name LIMIT 1")
    Product getProductByNameSync(String name); // --- TASK 1: ADD getProductByNameSync ---

    // ------------------------------------------------
    // Sync queue queries
    // ------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long addToSyncQueue(SyncQueue item);

    @Query("SELECT * FROM sync_queue WHERE synced = 0 ORDER BY createdAt ASC")
    List<SyncQueue> getUnsyncedItems();

    @Query("UPDATE sync_queue SET synced = 1 WHERE id IN (:ids)")
    void markAsSynced(List<Long> ids);
}
