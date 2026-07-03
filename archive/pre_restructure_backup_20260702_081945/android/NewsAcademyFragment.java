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

import ai.arena.hisabdar.modern1405.R;
import ai.arena.hisabdar.modern1405.viewmodel.MainViewModel;

/**
 * News Tab Controller integrating the groundbreaking 'آموزش هوش مصنوعی' (Firoozeh AI Micro-Academy - Nekte 3).
 * Feeds short, 30-second lessons on trade psychology, bazzar marketing, and inflation-safeguarding.
 */
public class NewsAcademyFragment extends Fragment {

    private MainViewModel viewModel;
    private View academySectionContainer;
    private TextView lessonTitleText;
    private TextView lessonSummaryText;
    private TextView lessonCategoryText;

    private int currentLessonIndex = 0;

    // Simulated local bazzar lessons database (matching the backend API design)
    private final String[][] lessons = {
            {
                    "💡 تله نقدینگی: سود اسمی در برابر سود واقعی بازار",
                    "حاجی‌جان! خیلی وقتا دفتر فیش نشون میده ۱۰۰ میلیون سود کردی، ولی وقتی میری جنس جدید بخری می‌بینی باید ۱۵۰ میلیون بذاری روش! این تله تورمه. همیشه قیمت فروش رو بر اساس قیمت روز بازار عمده (Replacement Cost) حساب کن، نه قیمتی که خودت خریدی.",
                    "مدیریت چرخه مالی حجره"
            },
            {
                    "🤝 وصول طلب بدون دلخوری در صنف سنتی",
                    "حرمت و آبرو تو بازار حرف اول رو می‌زنه. به جای عصبانیت و تندخویی با بدهکاران، از الگوهای یادآوری فیروزه استفاده کن. ابتدا به صورت ریش‌سفیدی پیش برو و با محبت یادآوری کن، بعد درخواست چک صیادی با موعد مناسب بده.",
                    "روانشناسی بازار"
            },
            {
                    "📦 خواب سرمایه انبار را نقد کنید",
                    "بیکار موندن بار توی قفسه‌ها، سم کشنده حجره است. اگر جنسی بیش از ۹۰ روز تکون نخورده، حتی با سود صفر یا ضرر جزیی نقدش کن. سود بازار توی سرعت گردش جنسه (Inventory Turnover) نه خوابوندن اون در امید گرون‌تر شدن.",
                    "مدیریت انبار و لجستیک"
            }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_academy, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        academySectionContainer = view.findViewById(R.id.academy_section_container);
        lessonTitleText = view.findViewById(R.id.lesson_title_text);
        lessonSummaryText = view.findViewById(R.id.lesson_summary_text);
        lessonCategoryText = view.findViewById(R.id.lesson_category_text);

        setupAcademyView();
        setupListeners(view);

        return view;
    }

    private void setupAcademyView() {
        String[] currentLesson = lessons[currentLessonIndex];
        lessonTitleText.setText(currentLesson[0]);
        lessonSummaryText.setText(currentLesson[1]);
        lessonCategoryText.setText("دسته: " + currentLesson[2]);
    }

    private void setupListeners(View view) {
        // Play audio simulator button
        view.findViewById(R.id.btn_play_audio_lesson).setOnClickListener(v -> {
            Toast.makeText(getContext(), "در حال پخش پادکست ۳۰ ثانیه‌ای صوتی بومی فیروزه...", Toast.LENGTH_SHORT).show();
        });

        // Next lesson button
        view.findViewById(R.id.btn_next_lesson).setOnClickListener(v -> {
            currentLessonIndex = (currentLessonIndex + 1) % lessons.length;
            setupAcademyView();
        });

        // Icon Click action to activate the interactive education portal
        view.findViewById(R.id.icon_ai_academy_badge).setOnClickListener(v -> {
            academySectionContainer.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "آکادمی هوشمند فیروزه فعال شد. علم کسب‌وکار بازار سنتی!", Toast.LENGTH_SHORT).show();
        });
    }
}
