package net.kdt.pojavlaunch;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kdt.mcgui.ProgressLayout;
import com.kdt.mcgui.mcAccountSpinner;

import net.kdt.pojavlaunch.fragments.MainMenuFragment;
import net.kdt.pojavlaunch.fragments.ModsFragment;
import net.kdt.pojavlaunch.fragments.ServersFragment;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment;

import java.lang.ref.WeakReference;

public class LauncherActivity extends BaseActivity {
    private ActivityResultLauncher<String> mRequestNotificationPermissionLauncher;
    private ActivityResultLauncher<String> mRequestMicrophonePermissionLauncher;
    private WeakReference<Runnable> mRequestNotificationPermissionRunnable;
    private WeakReference<Runnable> mRequestMicrophonePermissionRunnable;
    private BottomNavigationView mBottomNavigationView;
    private FragmentContainerView mFragmentView;

    public boolean checkForNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true;
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkForMicrophonePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public void askForNotificationPermission(Runnable onAllowed) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (onAllowed != null) onAllowed.run();
            return;
        }
        mRequestNotificationPermissionRunnable = new WeakReference<>(onAllowed);
        mRequestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    public void askForMicrophonePermission(Runnable onAllowed) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (onAllowed != null) onAllowed.run();
            return;
        }
        mRequestMicrophonePermissionRunnable = new WeakReference<>(onAllowed);
        mRequestMicrophonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestNotificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isAllowed -> {
                    if (isAllowed) {
                        Runnable runnable = Tools.getWeakReference(mRequestNotificationPermissionRunnable);
                        if (runnable != null) runnable.run();
                    } else {
                        LauncherPreferences.PREF_SKIP_NOTIFICATION_PERMISSION_CHECK = true;
                        LauncherPreferences.DEFAULT_PREF.edit()
                                .putBoolean(LauncherPreferences.PREF_KEY_SKIP_NOTIFICATION_PERMISSION_CHECK, true)
                                .apply();
                    }
                }
        );
        mRequestMicrophonePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isAllowed -> {
                    if (isAllowed) {
                        Runnable runnable = Tools.getWeakReference(mRequestMicrophonePermissionRunnable);
                        if (runnable != null) runnable.run();
                    }
                }
        );
    }

    private void setupBottomNavigation() {
        mBottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Tools.backToMainMenu(this);
                return true;
            } else if (id == R.id.nav_instances) {
                Tools.swapFragment(this, MainMenuFragment.class, "ROOT", null);
                return true;
            } else if (id == R.id.nav_mods) {
                Tools.swapFragment(this, ModsFragment.class, "MODS_FRAGMENT", null);
                return true;
            } else if (id == R.id.nav_servers) {
                Tools.swapFragment(this, ServersFragment.class, "SERVERS_FRAGMENT", null);
                return true;
            } else if (id == R.id.nav_settings) {
                Tools.swapFragment(this, LauncherPreferenceFragment.class, "SETTINGS_FRAGMENT", null);
                return true;
            }
            return false;
        });
    }
}
