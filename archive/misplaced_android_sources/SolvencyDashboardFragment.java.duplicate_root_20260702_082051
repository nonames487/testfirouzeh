package ai.arena.hisabdar.modern1405.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import ai.arena.hisabdar.modern1405.R;
import ai.arena.hisabdar.modern1405.util.BazaarCreditScoringEngine;
import ai.arena.hisabdar.modern1405.viewmodel.MainViewModel;

/**
 * Option 2: Three-Layered Graphical "Solvency Scale" Dashboard (ترازوی بقای حجره - v10.0.3).
 * Segregates financial state into 3 distinct cognitive layers:
 * 1. Present (حال): Real net cash flow and immediate liquid assets.
 * 2. Future (آینده): Debt aging brackets, bounced cheque risks, and client creditworthiness ratings.
 * 3. Growth (توسعه): Proactive AI purchasing alerts and external market trend notifications.
 */
public class SolvencyDashboardFragment extends Fragment {

    private MainViewModel viewModel;

    // UI elements
    private TextView txtPresentCashFlow;
    private TextView txtFutureCreditRisk;
    private TextView txtGrowthTrends;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_solvency_dashboard, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        txtPresentCashFlow = view.findViewById(R.id.txt_present_cash_flow);
        txtFutureCreditRisk = view.findViewById(R.id.txt_future_credit_risk);
        txtGrowthTrends = view.findViewById(R.id.txt_growth_trends);

        renderPresentLayer();
        renderFutureLayer();
        renderGrowthLayer();

        return view;
    }

    /**
     * Layer 1: Present (حال) — Liquid cash flow
     */
    private void renderPresentLayer() {
        long totalInflows = 750000000L;  // 750M Rials
        long totalOutflows = 420000000L; // 420M Rials
        long baseAdjustment = 50000000L; // 50M Rials
        
        long netCashFlow = totalInflows - totalOutflows;
        long trueBalance = netCashFlow + baseAdjustment;

        String text = "📊 جریان نقدی ورودی: " + String.format("%,d ریال", totalInflows) + "\n" +
                "💸 کل وجوه پرداختی: " + String.format("%,d ریال", totalOutflows) + "\n" +
                "🟢 جریان خالص نقدی (دخل واقعی): " + String.format("%,d ریال", netCashFlow) + "\n" +
                "💎 مانده نقدینگی نهایی صندوق: " + String.format("%,d ریال", trueBalance);
        
        txtPresentCashFlow.setText(text);
    }

    /**
     * Layer 2: Future (آینده) — Debt aging and creditworthines
     */
    private void renderFutureLayer() {
        // Evaluate credit score of best and worst customer using BazaarCreditScoringEngine
        List<Integer> delays = new ArrayList<>();
        delays.add(45); delays.add(60); delays.add(90);
        
        BazaarCreditScoringEngine.CreditReport report = BazaarCreditScoringEngine.evaluateOnDeviceCreditScore(
                "مرتضی رحیمی",
                1, // 1 Bounced Cheque
                delays,
                'D', // Bad reputational tier
                2 // only 2 successful repayments
        );

        String text = "👤 طرف حساب تحت ریسک: مرتضی رحیمی\n" +
                "🚨 نمره اعتبار بازار فیروزه: " + report.score + " از ۱۰۰۰ (بحرانی)\n" +
                "🔒 حداکثر سقف مجاز نسیه: " + String.format("%,d ریال", report.maxRecommendedCreditRials) + "\n" +
                "💡 توصیه فیروزه: " + report.recommendationMessage;
                
        txtFutureCreditRisk.setText(text);
    }

    /**
     * Layer 3: Growth (توسعه) — Proactive AI Market Trends
     */
    private void renderGrowthLayer() {
        String text = "📈 ترندهای ورودی از دیتاسنتر ابری فیروزه:\n" +
                "- تقاضای آهن‌آلات و میلگرد به دلیل برپایی نمایشگاه محلی عمران اصفهان ۲۵٪ رشد داشته است.\n" +
                "- بنکدار عمده قیمت برنج طارم را ۵٪ ارزان‌تر به صورت نقدی حراج کرده است.\n" +
                "💡 اقدام توسعه حجره: سرمایه نقدی آزاد شده صندوق را به خرید عمده برنج اختصاص دهید تا در فصل تقاضا سود فیروزه‌ای کسب کنید!";
                
        txtGrowthTrends.setText(text);
    }
}
