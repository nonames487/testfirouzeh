package ai.arena.hisabdar.modern1405.repository;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.arena.hisabdar.modern1405.BuildConfig;
import ai.arena.hisabdar.modern1405.db.AccountingDao;
import ai.arena.hisabdar.modern1405.db.AppDatabase;
import ai.arena.hisabdar.modern1405.db.Party;
import ai.arena.hisabdar.modern1405.db.Product;
import ai.arena.hisabdar.modern1405.db.SyncQueue;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SyncManager {

    private static final String TAG = "SyncManager";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

    private final Context context;
    private final AccountingDao accountingDao;
    private final ExecutorService databaseExecutor;
    private final Handler mainHandler;
    private final OkHttpClient okHttpClient;
    private final String backendBaseUrl;

    public SyncManager(Context context, String backendBaseUrl) {
        this.context = context.getApplicationContext();
        this.accountingDao = AppDatabase.getInstance(context).accountingDao();
        this.databaseExecutor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.okHttpClient = new OkHttpClient();
        this.backendBaseUrl = normalizeBaseUrl(backendBaseUrl);
    }

    public void triggerActiveSync(@NonNull OnSyncCompletedListener listener) {
        triggerActiveSync(0, listener);
    }

    public void triggerActiveSync(long lastSyncTimestamp, @NonNull OnSyncCompletedListener listener) {
        databaseExecutor.execute(() -> {
            if (backendBaseUrl.isEmpty()) {
                postFailure(listener, "Backend URL is not configured");
                return;
            }

            if (!isNetworkAvailable()) {
                postFailure(listener, "No network connection");
                return;
            }

            List<SyncQueue> unsyncedItems = accountingDao.getUnsyncedItems();
            if (unsyncedItems == null || unsyncedItems.isEmpty()) {
                Log.d(TAG, "No unsynced items to send.");
                postSuccess(listener, 0, 0);
                return;
            }

            Request request = buildSyncRequest(unsyncedItems, lastSyncTimestamp);
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled()) {
                        Log.e(TAG, "Sync failed", e);
                        postFailure(listener, "Sync failed: " + e.getMessage());
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        if (!response.isSuccessful() || response.body() == null) {
                            Log.e(TAG, "Server returned error: " + response.code());
                            postFailure(listener, "Server error: " + response.code());
                            return;
                        }

                        String responseBody = response.body().string();
                        JsonObject jsonResponse = GSON.fromJson(responseBody, JsonObject.class);
                        int processedCount = jsonResponse.has("processed_count")
                                ? jsonResponse.get("processed_count").getAsInt()
                                : 0;

                        databaseExecutor.execute(() -> {
                            int updatesApplied = applyUpdatesAndMarkSynced(jsonResponse, unsyncedItems);
                            postSuccess(listener, processedCount, updatesApplied);
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing server response", e);
                        postFailure(listener, "Error: " + e.getMessage());
                    } finally {
                        response.close();
                    }
                }
            });
        });
    }

    public void enqueueForSync(String entityType, String payloadJson) {
        databaseExecutor.execute(() -> {
            SyncQueue item = new SyncQueue(entityType, payloadJson, false, new Date().getTime());
            accountingDao.addToSyncQueue(item);
        });
    }

    private Request buildSyncRequest(List<SyncQueue> unsyncedItems, long lastSyncTimestamp) {
        List<JsonObject> clientLogs = new ArrayList<>();
        for (SyncQueue item : unsyncedItems) {
            JsonObject logEntry = new JsonObject();
            logEntry.addProperty("id", item.getId());
            logEntry.addProperty("entityType", item.getEntityType());
            logEntry.addProperty("createdAt", item.getCreatedAt());
            logEntry.addProperty("clientTimestamp", System.currentTimeMillis());
            logEntry.add("payloadJson", GSON.toJsonTree(item.getPayloadJson()));
            clientLogs.add(logEntry);
        }

        JsonObject requestBody = new JsonObject();
        requestBody.add("client_logs", GSON.toJsonTree(clientLogs));
        requestBody.addProperty("last_sync_timestamp", lastSyncTimestamp);

        RequestBody body = RequestBody.create(GSON.toJson(requestBody), JSON);
        return new Request.Builder()
                .url(backendBaseUrl + "/sync")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("X-API-Key", BuildConfig.SYNC_API_KEY)
                .build();
    }

    private int applyUpdatesAndMarkSynced(JsonObject jsonResponse, List<SyncQueue> unsyncedItems) {
        int updatesApplied = 0;
        try {
            if (jsonResponse.has("updates_for_client")) {
                JsonArray updates = jsonResponse.getAsJsonArray("updates_for_client");
                for (JsonElement updateElem : updates) {
                    JsonObject update = updateElem.getAsJsonObject();
                    String entityType = update.get("entityType").getAsString();
                    String payloadJson = update.get("payloadJson").toString();

                    if (payloadJson.startsWith("\"") && payloadJson.endsWith("\"")) {
                        payloadJson = payloadJson.substring(1, payloadJson.length() - 1).replace("\\\"", "\"");
                    }

                    if (mergeServerRecordIntoLocalDb(entityType, payloadJson)) {
                        updatesApplied++;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying updates", e);
        }

        List<Long> ids = new ArrayList<>();
        for (SyncQueue item : unsyncedItems) {
            ids.add(item.getId());
        }
        accountingDao.markAsSynced(ids);
        return updatesApplied;
    }

    private boolean mergeServerRecordIntoLocalDb(String entityType, String payloadJson) {
        try {
            switch (entityType.toLowerCase()) {
                case "party":
                    mergeParty(GSON.fromJson(payloadJson, Party.class));
                    return true;

                case "product":
                    mergeProduct(GSON.fromJson(payloadJson, Product.class));
                    return true;

                default:
                    Log.w(TAG, "Unknown entity type during merge: " + entityType);
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error merging " + entityType, e);
            return false;
        }
    }

    private void mergeParty(Party serverParty) {
        Party existingParty = accountingDao.getPartyById(serverParty.getId());
        if (existingParty != null) {
            existingParty.setName(serverParty.getName());
            existingParty.setPhone(serverParty.getPhone());
            existingParty.setNationalId(serverParty.getNationalId());
            existingParty.setBalance(serverParty.getBalance());
            accountingDao.updateParty(existingParty);
        } else {
            accountingDao.insertParty(serverParty);
        }
    }

    private void mergeProduct(Product serverProduct) {
        Product existingProduct = null;

        if (serverProduct.getId() > 0) {
            existingProduct = accountingDao.getProductById(serverProduct.getId());
        }

        if (existingProduct == null
                && serverProduct.getBarcode() != null
                && !serverProduct.getBarcode().isEmpty()) {
            existingProduct = accountingDao.findProductByBarcode(serverProduct.getBarcode());
        }

        if (existingProduct == null
                && serverProduct.getName() != null
                && !serverProduct.getName().isEmpty()) {
            existingProduct = accountingDao.getProductByNameSync(serverProduct.getName());
        }

        if (existingProduct != null) {
            existingProduct.setName(serverProduct.getName());
            existingProduct.setBarcode(serverProduct.getBarcode());
            existingProduct.setStock(serverProduct.getStock());
            existingProduct.setCost(serverProduct.getCost());
            existingProduct.setSalePrice(serverProduct.getSalePrice());
            existingProduct.setLastPurchasePrice(serverProduct.getLastPurchasePrice());
            existingProduct.setCategory(serverProduct.getCategory());
            accountingDao.updateProduct(existingProduct);
        } else {
            accountingDao.insertProduct(serverProduct);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            return capabilities != null
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }

    public void cleanup() {
        okHttpClient.dispatcher().cancelAll();
        mainHandler.removeCallbacksAndMessages(null);
        if (!databaseExecutor.isShutdown()) {
            Log.i(TAG, "Shutting down sync manager database executor...");
            databaseExecutor.shutdownNow();
        }
    }

    private void postSuccess(OnSyncCompletedListener listener, int processedCount, int updatesApplied) {
        mainHandler.post(() -> listener.onSuccess(processedCount, updatesApplied));
    }

    private void postFailure(OnSyncCompletedListener listener, String errorMessage) {
        mainHandler.post(() -> listener.onFailure(errorMessage));
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            return "";
        }

        String trimmed = baseUrl.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    public interface OnSyncCompletedListener {
        void onSuccess(int processedCount, int updatesApplied);
        void onFailure(String errorMessage);
    }
}
