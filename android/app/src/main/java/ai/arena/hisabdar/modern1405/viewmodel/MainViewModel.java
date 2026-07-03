package ai.arena.hisabdar.modern1405.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ai.arena.hisabdar.modern1405.BuildConfig;
import ai.arena.hisabdar.modern1405.repository.AccountingRepository;
import ai.arena.hisabdar.modern1405.repository.AiRepository;
import ai.arena.hisabdar.modern1405.repository.SyncManager;

public class MainViewModel extends AndroidViewModel {

    private final AccountingRepository repository;
    private final AiRepository aiRepository;
    private final SyncManager syncManager;

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
        this.aiRepository = new AiRepository(BuildConfig.BACKEND_BASE_URL);
        this.syncManager = new SyncManager(application, BuildConfig.BACKEND_BASE_URL);
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

    public LiveData<Float> getAiFeedbackScore() {
        return aiFeedbackScore;
    }

    public void askFiroozehAI(String question, long sales, long purchases, long inventory, long credits, long dueCheques) {
        aiRepository.askFiroozehAI(question, sales, purchases, inventory, credits, dueCheques,
                new AiRepository.OnAiResponseListener() {
                    @Override
                    public void onSuccess(String answer) {
                        firoozehAiResponse.postValue(answer);
                    }

                    @Override
                    public void onFailure(String error) {
                        errorMessage.postValue(error);
                    }
                });
    }

    public void submitAiFeedback(float score, String merchantNotes) {
        aiRepository.submitAiFeedback(score, merchantNotes, new AiRepository.OnAiFeedbackListener() {
            @Override
            public void onSuccess(String riskAppetite, float feedbackScore) {
                aiRiskAppetite.postValue(riskAppetite);
                aiFeedbackScore.postValue(feedbackScore);
            }

            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
            }
        });
    }

    public void triggerActiveSync(long lastSyncTimestamp) {
        isSyncing.setValue(true);
        syncManager.triggerActiveSync(lastSyncTimestamp, new SyncManager.OnSyncCompletedListener() {
            @Override
            public void onSuccess(int processedClientCount, int receivedServerCount) {
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

    public void parseVoiceOrTextInvoice(String invoiceText, String documentType, OnInvoiceParsedListener listener) {
        aiRepository.parseVoiceOrTextInvoice(invoiceText, documentType, new AiRepository.OnInvoiceParsedListener() {
            @Override
            public void onSuccess(String rawJson) {
                listener.onSuccess(rawJson);
            }

            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        aiRepository.cleanup();
        syncManager.cleanup();
        repository.cleanup();
    }

    public interface OnInvoiceParsedListener {
        void onSuccess(String rawJson);
    }
}
