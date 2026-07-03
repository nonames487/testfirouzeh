package ai.arena.hisabdar.modern1405.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.arena.hisabdar.modern1405.db.AccountingDao;
import ai.arena.hisabdar.modern1405.db.AppDatabase;
import ai.arena.hisabdar.modern1405.db.Party;
import ai.arena.hisabdar.modern1405.db.Product;

public class AccountingRepository {

    private static final String TAG = "AccountingRepository";

    private final AccountingDao accountingDao;
    private final ExecutorService databaseExecutor;

    public AccountingRepository(Context context) {
        this.accountingDao = AppDatabase.getInstance(context).accountingDao();
        this.databaseExecutor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Party>> getAllParties() {
        return accountingDao.getAllParties();
    }

    public LiveData<List<Party>> searchPartiesByPrefix(String query) {
        return accountingDao.searchPartiesByPrefix(query);
    }

    public LiveData<List<Product>> getAllProducts() {
        return accountingDao.getAllProducts();
    }

    public LiveData<List<Product>> searchProductsByPrefix(String query) {
        return accountingDao.searchProductsByPrefix(query);
    }

    public void insertParty(Party party) {
        databaseExecutor.execute(() -> accountingDao.insertParty(party));
    }

    public void updateParty(Party party) {
        databaseExecutor.execute(() -> accountingDao.updateParty(party));
    }

    public void insertProduct(Product product) {
        databaseExecutor.execute(() -> accountingDao.insertProduct(product));
    }

    public void updateProduct(Product product) {
        databaseExecutor.execute(() -> accountingDao.updateProduct(product));
    }

    public void cleanup() {
        if (!databaseExecutor.isShutdown()) {
            Log.i(TAG, "Shutting down accounting repository database executor...");
            databaseExecutor.shutdownNow();
        }
    }
}
