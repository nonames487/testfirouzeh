package ai.arena.hisabdar.modern1405.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ai.arena.hisabdar.modern1405.R;
import ai.arena.hisabdar.modern1405.viewmodel.MainViewModel;

/**
 * Home Controller implementing Both Studio Mode and Classic Mode (Toggleable).
 * Connects directly to MainViewModel to observe real-time bazaar metrics (Sales, Profit, Cash).
 */
public class HomeFragment extends Fragment {

    private MainViewModel viewModel;
    private boolean isStudioMode = true; // Default toggle state

    private TextView cashBalanceText;
    private View studioModeContainer;
    private View classicModeContainer;
    private Button toggleModeButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        cashBalanceText = view.findViewById(R.id.cash_balance_text);
        studioModeContainer = view.findViewById(R.id.studio_mode_container);
        classicModeContainer = view.findViewById(R.id.classic_mode_container);
        toggleModeButton = view.findViewById(R.id.btn_toggle_mode);

        setupModeViews();
        setupListeners(view);
        observeData();

        return view;
    }

    private void setupModeViews() {
        if (isStudioMode) {
            studioModeContainer.setVisibility(View.VISIBLE);
            classicModeContainer.setVisibility(View.GONE);
            toggleModeButton.setText("تغییر به منوی کامل (Classic)");
        } else {
            studioModeContainer.setVisibility(View.GONE);
            classicModeContainer.setVisibility(View.VISIBLE);
            toggleModeButton.setText("تغییر به نمای ساده (Studio)");
        }
    }

    private void setupListeners(View view) {
        // Toggle view button
        toggleModeButton.setOnClickListener(v -> {
            isStudioMode = !isStudioMode;
            setupModeViews();
        });

        // Quick AI buttons in Studio Mode
        view.findViewById(R.id.btn_ai_sale).setOnClickListener(v -> {
            // Trigger Voice OCR Dialog or Quick Invoice OCR
            Toast.makeText(getContext(), "ثبت فاکتور فروش هوشمند با هوش مصنوعی فیروزه", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btn_ai_buy).setOnClickListener(v -> {
            Toast.makeText(getContext(), "ثبت فاکتور خرید هوشمند", Toast.LENGTH_SHORT).show();
        });

        // AI Consultation trigger
        view.findViewById(R.id.btn_ai_consulting).setOnClickListener(v -> {
            // Opens the modern AI Consulting Room Dialog
            ConsultingRoomFragment dialog = new ConsultingRoomFragment();
            dialog.show(getParentFragmentManager(), "ConsultingRoom");
        });
    }

    private void observeData() {
        viewModel.getCashBalance().observe(getViewLifecycleOwner(), balance -> {
            if (balance != null) {
                cashBalanceText.setText(String.format("%,d ریال", balance));
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
