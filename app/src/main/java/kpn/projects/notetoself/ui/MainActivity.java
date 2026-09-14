package kpn.projects.notetoself.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import kpn.projects.notetoself.R;
import kpn.projects.notetoself.notifications.NotificationAlarmScheduler;
import kpn.projects.notetoself.notifications.NotificationRefreshReceiver;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startStickyNotificationService();
                }
                // denied is a valid choice — app still works, just no sticky notification
                // until the user grants it later via system Settings
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View rootView = findViewById(R.id.main);
        View navHostContainer = findViewById(R.id.nav_host_fragment);
        View bottomNav = findViewById(R.id.bottom_nav);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            navHostContainer.setPadding(navHostContainer.getPaddingLeft(), systemBars.top,
                    navHostContainer.getPaddingRight(), navHostContainer.getPaddingBottom());

            bottomNav.setPadding(bottomNav.getPaddingLeft(), bottomNav.getPaddingTop(),
                    bottomNav.getPaddingRight(), systemBars.bottom);

            return insets;
        });

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(((FragmentContainerView) navHostContainer).getId());
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController((com.google.android.material.bottomnavigation.BottomNavigationView) bottomNav, navController);

        requestNotificationPermissionIfNeeded();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                startStickyNotificationService();
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            startStickyNotificationService(); // pre-Android 13 — granted at install time, no prompt needed
        }
    }

    private void startStickyNotificationService() {
        NotificationAlarmScheduler.ensureScheduled(this);
        NotificationRefreshReceiver.triggerImmediateRefresh(this);
    }
}