package ai.arena.hisabdar.modern1405.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.arena.hisabdar.modern1405.db.Party;
import ai.arena.hisabdar.modern1405.db.Product;
import ai.arena.hisabdar.modern1405.repository.AccountingRepository;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class MainViewModel extends AndroidViewModel {

    private final AccountingRepository repository;
    private final OkHttpClient okHttpClient;
    private static final String BASE_URL = "http://YOUR_BACKEND_IP:8000";

    // LiveData states
    private final MutableLiveData<Long> cashBalance = new MutableLiveData<>(0L);
    private final MutableLiveData<String> firoozehAiResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    // Nekte 2 & 7: Feedback and Sync telemetry states
    private final MutableLiveData<String> aiRiskAppetite = new MutableLiveData<>("محافظه‌کار");
    private final MutableLiveData<Float> aiFeedbackScore = new MutableLiveData<>(0.0f);
    private final MutableLiveData<String> syncResultMessage = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.repository = new AccountingRepository(application);
        this.okHttpClient = new OkHttpClient();
    }

    public LiveData<Long> getCashBalance() {
        return cashBalance;
    }

    public LiveData<String> getFiroozehAiResponse() {
        return firoozehAiResponse;
    }

    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getAiRiskAppetite() {
        return aiRiskAppetite;
    }

    public LiveData<String> getSyncResultMessage() {
        return syncResultMessage;
    }

    /**
     * Integrates with Python FastAPI /chat endpoint.
     * Packages current inventory, sales, credits, and cheques and sends them to AI for personalized advice.
     */
    public void askFiroozehAI(String question, long sales, long purchases, long inventory, long credits, long dueCheques) {
        try {
            JSONObject totalsObj = new JSONObject();
            totalsObj.put("sales", sales);
            totalsObj.put("purchases", purchases);
            totalsObj.put("inventory", inventory);
            totalsObj.put("credits", credits);
            totalsObj.put("dueCheques", dueCheques);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("question", question);
            jsonBody.put("totals", totalsObj);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + "/chat")
                    .post(body)
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    errorMessage.postValue("عدم برقراری ارتباط با مشاور هوشمند فیروزه!");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JSONObject resObj = new JSONObject(response.body().string());
                            String answer = resObj.getString("answer");
                            firoozehAiResponse.postValue(answer);
                        } catch (JSONException e) {
                            errorMessage.postValue("خطا در پردازش پاسخ فیروزه!");
                        }
                    } else {
                        errorMessage.postValue("پاسخ ناموفق از سرور فیروزه!");
                    }
                }
            });

        } catch (JSONException e) {
            errorMessage.setValue("خطا در تنظیم پیام هوش مصنوعی!");
        }
    }

    /**
     * Nekte 7: Submits reinforcement learning feedback (score -1.0 to +1.0)
     * to the Firoozeh AI Engine on the server to adapt its profile and advice.
     */
    public void submitAiFeedback(float score, String merchantNotes) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("score", score);
            jsonBody.put("merchant_notes", merchantNotes);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + "/chat/feedback")
                    .post(body)
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    errorMessage.postValue("خطا در ثبت فیدبک هوش مصنوعی!");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JSONObject resObj = new JSONObject(response.body().string());
                            String risk = resObj.getString("new_risk_appetite");
                            float currentScore = (float) resObj.getDouble("current_score");
                            
                            aiRiskAppetite.postValue(risk);
                            aiFeedbackScore.postValue(currentScore);
                        } catch (JSONException e) {
                            errorMessage.postValue("خطا در به‌روزرسانی مشخصات هوش مصنوعی!");
                        }
                    }
                }
            });
        } catch (JSONException e) {
            errorMessage.setValue("خطا در تنظیم درخواست بازخورد!");
        }
    }

    /**
     * Nekte 1: Triggers the robust Active Synchronization Handshake
     * using the accounting repository.
     */
    public void triggerActiveSync(long lastSyncTimestamp) {
        isSyncing.setValue(true);
        repository.triggerActiveSync(lastSyncTimestamp, new AccountingRepository.OnSyncCompletedListener() {
            @Override
            public void onSuccess(int processedClientCount, int receivedServerCount, long serverSyncTimestamp) {
                isSyncing.postValue(false);
                syncResultMessage.postValue(
                    "همگام‌سازی با موفقیت انجام شد! " + processedClientCount + " آیتم ارسال و " +
                    receivedServerCount + " به‌روزرسانی از ابر مرکزی دریافت شد."
                );
            }

            @Override
            public void onFailure(String error) {
                isSyncing.postValue(false);
                errorMessage.postValue("خطا در همگام‌سازی: " + error);
            }
        });
    }

    /**
     * Calls FastAPI endpoint to analyze textual description of invoice.
     */
    public void parseVoiceOrTextInvoice(String invoiceText, String documentType, OnInvoiceParsedListener listener) {
        RequestBody formBody = new FormBody.Builder()
                .add("text", invoiceText)
                .add("documentType", documentType)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/analyze-invoice-text")
                .post(formBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                errorMessage.postValue("خطای ارتباط با سیستم خواندن فاکتور صوتی!");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String rawJson = response.body().string();
                        new Handler(Looper.getMainLooper()).post(() -> listener.onSuccess(rawJson));
                    } catch (Exception e) {
                        errorMessage.postValue("خطا در بازخوانی متن فاکتور!");
                    }
                } else {
                    errorMessage.postValue("سرور فاکتور صوتی با خطا مواجه شد!");
                }
            }
        });
    }

    public interface OnInvoiceParsedListener {
        void onSuccess(String rawJson);
    }
}
