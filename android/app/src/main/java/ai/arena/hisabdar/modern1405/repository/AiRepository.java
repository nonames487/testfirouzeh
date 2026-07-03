package ai.arena.hisabdar.modern1405.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiRepository {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient;
    private final Handler mainHandler;
    private final String backendBaseUrl;

    public AiRepository(String backendBaseUrl) {
        this.okHttpClient = new OkHttpClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.backendBaseUrl = normalizeBaseUrl(backendBaseUrl);
    }

    public void askFiroozehAI(
            String question,
            long sales,
            long purchases,
            long inventory,
            long credits,
            long dueCheques,
            OnAiResponseListener listener
    ) {
        if (backendBaseUrl.isEmpty()) {
            postFailure(listener::onFailure, "Backend URL is not configured");
            return;
        }

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

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(backendBaseUrl + "/chat")
                    .post(body)
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled()) {
                        postFailure(listener::onFailure, "عدم برقراری ارتباط با مشاور هوشمند فیروزه!");
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            JSONObject resObj = new JSONObject(response.body().string());
                            String answer = resObj.getString("answer");
                            postSuccess(() -> listener.onSuccess(answer));
                        } else {
                            postFailure(listener::onFailure, "پاسخ ناموفق از سرور فیروزه!");
                        }
                    } catch (JSONException e) {
                        postFailure(listener::onFailure, "خطا در پردازش پاسخ فیروزه!");
                    } finally {
                        response.close();
                    }
                }
            });
        } catch (JSONException e) {
            postFailure(listener::onFailure, "خطا در تنظیم پیام هوش مصنوعی!");
        }
    }

    public void submitAiFeedback(float score, String merchantNotes, OnAiFeedbackListener listener) {
        if (backendBaseUrl.isEmpty()) {
            postFailure(listener::onFailure, "Backend URL is not configured");
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("score", score);
            jsonBody.put("merchant_notes", merchantNotes);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(backendBaseUrl + "/chat/feedback")
                    .post(body)
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled()) {
                        postFailure(listener::onFailure, "خطا در ثبت فیدبک هوش مصنوعی!");
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            JSONObject resObj = new JSONObject(response.body().string());
                            String riskAppetite = resObj.getString("new_risk_appetite");
                            float feedbackScore = (float) resObj.getDouble("current_score");
                            postSuccess(() -> listener.onSuccess(riskAppetite, feedbackScore));
                        } else {
                            postFailure(listener::onFailure, "پاسخ ناموفق از سرور بازخورد فیروزه!");
                        }
                    } catch (JSONException e) {
                        postFailure(listener::onFailure, "خطا در به‌روزرسانی مشخصات هوش مصنوعی!");
                    } finally {
                        response.close();
                    }
                }
            });
        } catch (JSONException e) {
            postFailure(listener::onFailure, "خطا در تنظیم درخواست بازخورد!");
        }
    }

    public void parseVoiceOrTextInvoice(String invoiceText, String documentType, OnInvoiceParsedListener listener) {
        if (backendBaseUrl.isEmpty()) {
            postFailure(listener::onFailure, "Backend URL is not configured");
            return;
        }

        RequestBody formBody = new FormBody.Builder()
                .add("text", invoiceText)
                .add("documentType", documentType)
                .build();

        Request request = new Request.Builder()
                .url(backendBaseUrl + "/analyze-invoice-text")
                .post(formBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (!call.isCanceled()) {
                    postFailure(listener::onFailure, "خطای ارتباط با سیستم خواندن فاکتور صوتی!");
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String rawJson = response.body().string();
                        postSuccess(() -> listener.onSuccess(rawJson));
                    } else {
                        postFailure(listener::onFailure, "سرور فاکتور صوتی با خطا مواجه شد!");
                    }
                } finally {
                    response.close();
                }
            }
        });
    }

    public void cleanup() {
        okHttpClient.dispatcher().cancelAll();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private void postSuccess(Runnable runnable) {
        mainHandler.post(runnable);
    }

    private void postFailure(FailureCallback callback, String errorMessage) {
        mainHandler.post(() -> callback.onFailure(errorMessage));
    }

    private interface FailureCallback {
        void onFailure(String errorMessage);
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

    public interface OnAiResponseListener {
        void onSuccess(String answer);
        void onFailure(String errorMessage);
    }

    public interface OnAiFeedbackListener {
        void onSuccess(String riskAppetite, float feedbackScore);
        void onFailure(String errorMessage);
    }

    public interface OnInvoiceParsedListener {
        void onSuccess(String rawJson);
        void onFailure(String errorMessage);
    }
}
