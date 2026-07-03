package ai.arena.hisabdar.modern1405.util;

import java.util.List;

/**
 * Advanced, state-of-the-art Bazaar Credit-Scoring Engine (Nekte 2 / Gam-e-Badi).
 * Custom-tailored for the Iranian market conditions. Evaluates creditworthiness on-device:
 * - Cheque Bounces (تاثیر چک برگشتی)
 * - Average Payment Delay Days (روزهای دیرکرد حساب دفتری)
 * - Bazaar Reputational Tier (ضریب سنتی آبرو و امین بودن)
 * - Success Repayment Ratio (نرخ تسویه‌های موفق)
 */
public class BazaarCreditScoringEngine {

    public enum CreditRiskLevel {
        EXCELLENT_AMIN,     // عالی (امین صنف)
        SAFE_MOTAMAD,       // سالم و مطمئن
        LOW_MEDIUM_RISK,    // ریسک کم تا متوسط
        CRITICAL_BAD_DEBT   // بحرانی (بد حساب)
    }

    public static class CreditReport {
        public final int score; // 0 to 1000
        public final CreditRiskLevel riskLevel;
        public final long maxRecommendedCreditRials;
        public final String recommendationMessage;

        public CreditReport(int score, CreditRiskLevel riskLevel, long maxRecommendedCreditRials, String recommendationMessage) {
            this.score = score;
            this.riskLevel = riskLevel;
            this.maxRecommendedCreditRials = maxRecommendedCreditRials;
            this.recommendationMessage = recommendationMessage;
        }
    }

    /**
     * Calculates the credit score using modern statistical weights adapted for Iranian trade psychology.
     */
    public static CreditReport evaluateOnDeviceCreditScore(
            String partyName,
            int bouncedChequeCount,
            List<Integer> delayDaysHistory,
            char trustCircleTier, // 'A': Amin, 'B': Motamad, 'C': Ordinary, 'D': Red-flagged
            int successfulRepaymentsCount
    ) {
        int score = 600; // Base score

        // 1. Penalty for Bounced Cheques (چک برگشتی بزرگترین خط قرمز بازار است)
        score -= bouncedChequeCount * 120;

        // 2. Delay Days History Analysis
        if (delayDaysHistory != null && !delayDaysHistory.isEmpty()) {
            double sum = 0;
            for (int days : delayDaysHistory) {
                sum += days;
            }
            double averageDelay = sum / delayDaysHistory.size();

            if (averageDelay > 45) {
                score -= 150; // high payment inertia
            } else if (averageDelay > 15) {
                score -= 50;
            } else {
                score += 50;  // prompt payment bonus
            }
        }

        // 3. Bazaar Reputational Peer Circle (ضریب سنتی آبرو و معتمد صنف بودن)
        switch (Character.toUpperCase(trustCircleTier)) {
            case 'A': // Amin Bazaar
                score += 200;
                break;
            case 'B': // Motamad
                score += 100;
                break;
            case 'D': // Red-flagged
                score -= 200;
                break;
            case 'C':
            default:
                break;
        }

        // 4. Successful local repayment history
        score += Math.min(successfulRepaymentsCount * 15, 100);

        // Clamp score between 0 and 1000
        score = Math.max(0, Math.min(score, 1000));

        // Determine Risk levels and maximum suggested credits
        CreditRiskLevel riskLevel;
        long maxCredit;
        String recMsg;

        if (score >= 850) {
            riskLevel = CreditRiskLevel.EXCELLENT_AMIN;
            maxCredit = 1500000000L; // 1.5 Billion Rials (۱۵۰ میلیون تومان)
            recMsg = "✅ طرف حساب امین بازار بزرگ است. اعتبار فوق‌العاده بالا. می‌توانید تا سقف مبالغ سنگین به وی فروش نسیه یا چکی داشته باشید.";
        } else if (score >= 700) {
            riskLevel = CreditRiskLevel.SAFE_MOTAMAD;
            maxCredit = 800000000L; // 800 Million Rials
            recMsg = "این فرد دارای حساب منظم است و تاخیر بدهی‌های دفتری وی زیر ۱۵ روز است. گرفتن چک صیادی توصیه می‌شود.";
        } else if (score >= 500) {
            riskLevel = CreditRiskLevel.LOW_MEDIUM_RISK;
            maxCredit = 300000000L; // 300 Million Rials
            recMsg = "کاسب معمولی بازار. پرداخت‌ها با نوسان جزئی همراه است. مبنای فروش نسیه فقط به صورت محدود و منوط به دریافت چک صیادی ثبتی باشد.";
        } else {
            riskLevel = CreditRiskLevel.CRITICAL_BAD_DEBT;
            maxCredit = 0L; // No credit
            recMsg = "🚨 آژیر قرمز بد حسابی! فروش نسیه، دفتری یا موعددار به این شخص دارای ریسک فوق‌العاده بالایی از سوخت شدن سرمایه است. فقط معاملات نقدی!";
        }

        return new CreditReport(score, riskLevel, maxCredit, recMsg);
    }
}
