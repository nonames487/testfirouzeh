package ai.arena.hisabdar.modern1405.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ai.arena.hisabdar.modern1405.R; // R represents standard layout resources
import ai.arena.hisabdar.modern1405.viewmodel.MainViewModel;

/**
 * Modern Android Controller replacing the obsolete LegacyMainActivity.
 * Manages full-screen Fragment transactions, Bottom Navigation,
 * and Live Sync Status Badges.
 */
public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private View offlineBadge;
    private View syncProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Uses the professional Dark Theme: Background #061A17
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        offlineBadge = findViewById(R.id.offline_badge);
        syncProgressBar = findViewById(R.id.sync_progress_bar);

        setupBottomNavigation();
        observeSyncStatus();

        // Load Default Fragment (HomeFragment in Studio Mode)
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupBottomNavigation() {
        // BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        // navigation.setOnNavigationItemSelectedListener(item -> { ... });
    }

    private void observeSyncStatus() {
        viewModel.getIsSyncing().observe(this, isSyncing -> {
            if (isSyncing != null) {
                syncProgressBar.setVisibility(isSyncing ? View.VISIBLE : View.GONE);
                // Also updates online/offline badge based on sync state
                offlineBadge.setVisibility(isSyncing ? View.GONE : View.VISIBLE);
            }
        });
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
