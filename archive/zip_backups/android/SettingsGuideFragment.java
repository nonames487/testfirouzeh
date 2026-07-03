package ai.arena.hisabdar.modern1405.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ai.arena.hisabdar.modern1405.R;

/**
 * Settings Sub-Module: 'راهنمای کامل حسابدار فیروزه' (Interactive User Manual with ASCII diagrams).
 * Provides step-by-step guidance and structural flowcharts for each of our advanced AI modules.
 */
public class SettingsGuideFragment extends Fragment {

    private View guideMenuContainer;
    private View guideDetailContainer;
    private TextView guideTitleText;
    private TextView guideDiagramText;
    private TextView guideDescriptionText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_guide, container, false);

        guideMenuContainer = view.findViewById(R.id.guide_menu_container);
        guideDetailContainer = view.findViewById(R.id.guide_detail_container);
        guideTitleText = view.findViewById(R.id.guide_title_text);
        guideDiagramText = view.findViewById(R.id.guide_diagram_text);
        guideDescriptionText = view.findViewById(R.id.guide_description_text);

        setupMenuClickListeners(view);

        view.findViewById(R.id.btn_back_to_guide_menu).setOnClickListener(v -> {
            guideDetailContainer.setVisibility(View.GONE);
            guideMenuContainer.setVisibility(View.VISIBLE);
        });

        return view;
    }

    private void setupMenuClickListeners(View view) {
        // --- 1. Guide: Studio Mode vs Classic Mode ---
        view.findViewById(R.id.icon_guide_home).setOnClickListener(v -> showDetail(
                "🏠 راهنمای صفحه خانه (دوحالته)",
                "  +---------------------------------------+\n" +
                "  |               صفحه اصلی               |\n" +
                "  +---------------------------------------+\n" +
                "  | [Studio Mode]      | [Classic Mode]   |\n" +
                "  |   * دکمه‌های بزرگ    |   * کل آیکون‌ها     |\n" +
                "  |   * فروش/خرید صوتی  |   * انبار، چک‌ها    |\n" +
                "  |   * خلاصه دخل و سود |   * گزارش‌های تفصیلی |\n" +
                "  +--------------------+------------------+",
                "خلاصه کارکرد:\n" +
                "بازاری سنتی معمولاً وقت سرکله زدن با منوهای تو در تو را ندارد.\n" +
                "۱. در 'نمای ساده (Studio Mode)' فقط دکمه‌های بزرگ برای خرید/فروش با هوش مصنوعی و ۳ شاخص کلیدی انبار، سود و دخل نقدی روزانه را دارید.\n" +
                "۲. در 'منوی کامل (Classic Mode)' تمامی ابزارها مانند چک‌ها، بدهکار/بستانکار و دفتر سنتی در کاشی‌های مجزا در دسترس شما هستند."
        ));

        // --- 2. Guide: AI Invoice Processing (Voice and Photo) ---
        view.findViewById(R.id.icon_guide_ocr).setOnClickListener(v -> showDetail(
                "📸 راهنمای خواندن هوشمند فاکتورها (صوتی/عکس)",
                "  [عکس فاکتور] ---> (پردازش ابری) ---> [Claude Vision AI]\n" +
                "                                           |\n" +
                "  [اصلاح صوتی] <--- (استخراج دقیق اقلام) <---+\n" +
                "       |\n" +
                "       v\n" +
                "  [ثبت نهایی در دیتابیس Room کلاینت]",
                "خلاصه کارکرد:\n" +
                "برای بازاری‌هایی که مایل به فاکتور زدن دستی نیستند:\n" +
                "۱. از فاکتور کاغذی دست‌نویس عکس بگیرید. هوش مصنوعی فیروزه فوراً اقلام، قیمت‌ها و جمع کل را تفکیک و به ریال تبدیل می‌کند.\n" +
                "۲. اگر مشکلی در فاکتور بود، کافی است دکمه میکروفون را بزنید و به زبان ساده بگویید: 'تعداد ردیف اول رو ۲ برابر کن'. سیستم به صورت کاملاً صوتی تغییرات را روی جدول فاکتور اعمال می‌کند."
        ));

        // --- 3. Guide: AI Consulting Room ---
        view.findViewById(R.id.icon_guide_consulting).setOnClickListener(v -> showDetail(
                "💬 راهنمای اتاق مشاوره هوش مصنوعی و کپی امن",
                "  +-----------------------------------------+\n" +
                "  |             اتاق مشاوره AI              |\n" +
                "  +-----------------------------------------+\n" +
                "  |  [کپی امن]    ----->   [کپی کامل]        |\n" +
                "  |  (حذف نام اشخاص)      (حاوی تمام جزییات) |\n" +
                "  |                                         |\n" +
                "  |  [فیروزه بومی]   [ChatGPT]   [Claude]   |\n" +
                "  +-----------------------------------------+",
                "خلاصه کارکرد:\n" +
                "این بخش قلب مشاوره مالی حجره شماست.\n" +
                "۱. ابتدا دکمه 'کپی امن' را بزنید. هوش مصنوعی فیروزه خلاصه آمارهای دخل، فروش، انبار و طلب‌ها را بدون فاش کردن نام یا شماره مشتریان در حافظه کپی می‌کند تا حریم خصوصی تجاری شما در کارهای ابری ۱۰۰٪ حفظ شود.\n" +
                "۲. یکی از هوش مصنوعی‌های مطرح بازار (فیروزه بومی، ChatGPT، Claude) را انتخاب کنید تا بهترین راهنمایی تجاری را متناسب با بازار ایران دریافت کنید."
        ));

        // --- 4. Guide: Active Sync & Offline Mode ---
        view.findViewById(R.id.icon_guide_sync).setOnClickListener(v -> showDetail(
                "🔄 راهنمای همگام‌سازی واقعی و دیتاسنتر هیبریدی",
                "  (گوشی بازاری)             (دیتاسنتر ابری مرکزی)\n" +
                "  [کلاینت آفلاین]            [پایگاه داده پایدار]\n" +
                "   Room DB                     PostgreSQL\n" +
                "      |                            |\n" +
                "      +---> (ثبت در صف آفلاین)       |\n" +
                "      |                            |\n" +
                "      +=======> (سینک دوطرفه LWW) ===>",
                "خلاصه کارکرد:\n" +
                "۱. فیروزه کاملاً آفلاین‌فرست (Offline-First) است. در بازار با قطع شدن اینترنت، برنامه قطع نمی‌شود؛ تراکنش‌ها در صف گوشی ذخیره می‌شوند.\n" +
                "۲. به محض اولین اتصال به اینترنت، سیستم سینک با الگوریتم Last-Write-Wins تراکنش‌های محلی را با دیتاسنتر مرکزی یکپارچه کرده و تغییرات جدید ابری را روی گوشی شما اعمال می‌کند."
        ));
    }

    private void showDetail(String title, String diagram, String description) {
        guideMenuContainer.setVisibility(View.GONE);
        guideDetailContainer.setVisibility(View.VISIBLE);

        guideTitleText.setText(title);
        guideDiagramText.setText(diagram);
        guideDescriptionText.setText(description);
    }
}
