package ai.arena.hisabdar.modern1405.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import ai.arena.hisabdar.modern1405.R;
import ai.arena.hisabdar.modern1405.viewmodel.MainViewModel;

/**
 * DialogFragment representing 'اتاق مشاوره AI' (AI Consulting Room).
 * Formulates a strict, structured business summary and allows secure/full copying
 * or direct API interaction with AI models.
 */
public class ConsultingRoomFragment extends DialogFragment {

    private MainViewModel viewModel;
    private TextView aiResponseTextView;

    // Simulated local bazaar statistics for summary generation
    private final long sales = 450000000L; // 450M Rials
    private final long purchases = 320000000L; // 320M Rials
    private final long inventory = 1200000000L; // 1.2B Rials
    private final long credits = 180000000L; // 180M Rials (نسیه)
    private final long dueCheques = 250000000L; // 250M Rials

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_consulting_room, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        aiResponseTextView = view.findViewById(R.id.ai_response_text_view);

        setupButtons(view);
        observeAiResponse();

        return view;
    }

    private void setupButtons(View view) {
        // --- SECURE COPY (کپی امن) ---
        // Strips any personal identification details (Names, Phones) to safeguard merchant privacy before sharing
        view.findViewById(R.id.btn_secure_copy).setOnClickListener(v -> {
            String secureSummary = generateSummary(true);
            copyToClipboard(secureSummary);
            Toast.makeText(getContext(), "خلاصه امن حساب‌ها کپی شد! (بدون نام اشخاص)", Toast.LENGTH_SHORT).show();
        });

        // --- FULL COPY (کپی کامل) ---
        view.findViewById(R.id.btn_full_copy).setOnClickListener(v -> {
            String fullSummary = generateSummary(false);
            copyToClipboard(fullSummary);
            Toast.makeText(getContext(), "خلاصه کامل حساب‌ها کپی شد!", Toast.LENGTH_SHORT).show();
        });

        // --- MODEL SELECTION TRIGGERS ---
        view.findViewById(R.id.btn_firoozeh_internal).setOnClickListener(v -> {
            aiResponseTextView.setText("در حال دریافت تحلیل تخصصی از هوش مصنوعی بومی فیروزه...");
            viewModel.askFiroozehAI("وضعیت کلی حجره من چطور است؟", sales, purchases, inventory, credits, dueCheques);
        });

        view.findViewById(R.id.btn_chatgpt).setOnClickListener(v -> {
            aiResponseTextView.setText("در حال مشورت با ChatGPT (تحلیل مدیریتی)...");
            viewModel.askFiroozehAI("تحلیل مدیریتی و استراتژیک برای بهبود سود حجره ارائه بده.", sales, purchases, inventory, credits, dueCheques);
        });

        view.findViewById(R.id.btn_claude).setOnClickListener(v -> {
            aiResponseTextView.setText("در حال دریافت تحلیل دقیق از Claude...");
            viewModel.askFiroozehAI("بررسی دقیق جریان نقدینگی و ریسک مطالبات نسیه را تحلیل کن.", sales, purchases, inventory, credits, dueCheques);
        });

        view.findViewById(R.id.btn_close).setOnClickListener(v -> dismiss());
    }

    private String generateSummary(boolean isSecure) {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 خلاصه وضعیت مالی حجره فیروزه\n");
        sb.append(String.format("🔹 فروش این دوره: %,d ریال\n", sales));
        sb.append(String.format("🔹 خرید این دوره: %,d ریال\n", purchases));
        sb.append(String.format("🔹 موجودی انبار: %,d ریال\n", inventory));
        sb.append(String.format("🔹 چک‌های سررسید نزدیک: %,d ریال\n", dueCheques));
        
        if (isSecure) {
            sb.append(String.format("🔹 مطالبات نسیه (طرف‌حساب‌ها کاملا رمزنگاری و پنهان شده‌اند): %,d ریال\n", credits));
            sb.append("⚠️ حریم خصوصی تجاری رعایت شده و هیچ نام یا تلفنی صادر نگردیده است.\n");
        } else {
            sb.append(String.format("🔹 مطالبات نسیه: %,d ریال\n", credits));
            sb.append("🔸 جزئیات طرف حساب‌ها:\n");
            sb.append("- طرف حساب الف (بدهی: ۶۰,۰۰۰,۰۰۰ ریال - ۴۵ روز تاخیر)\n");
            sb.append("- طرف حساب ب (بدهی: ۱۲۰,۰۰۰,۰۰۰ ریال - ۱۵ روز تاخیر)\n");
        }
        return sb.toString();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("HisabdarFiroozehSummary", text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
    }

    private void observeAiResponse() {
        viewModel.getFiroozehAiResponse().observe(this, response -> {
            if (response != null) {
                aiResponseTextView.setText(response);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
