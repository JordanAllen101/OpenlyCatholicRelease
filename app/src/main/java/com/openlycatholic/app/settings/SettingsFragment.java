package com.openlycatholic.app.settings;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.openlycatholic.app.R;
import com.openlycatholic.app.examination.ConfessionReminderFragment;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {

    private TextView updateStatus;
    private CardView checkUpdatesCard;
    private boolean checkingForUpdates;
    private File pendingUpdateApk;

    private final ActivityResultLauncher<Intent> installPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                                && requireContext().getPackageManager()
                                .canRequestPackageInstalls()
                                && pendingUpdateApk != null) {
                            installUpdate(pendingUpdateApk);
                        } else if (updateStatus != null) {
                            updateStatus.setText(
                                    "Allow app installs to continue, then check again."
                            );
                        }
                    }
            );

    public SettingsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_settings,
                container,
                false
        );

        CardView confessionReminderCard = view.findViewById(R.id.confessionReminderCard);
        CardView passwordProtectionCard = view.findViewById(R.id.passwordProtectionCard);
        checkUpdatesCard = view.findViewById(R.id.checkUpdatesCard);
        updateStatus = view.findViewById(R.id.updateStatus);

        confessionReminderCard.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.mainContainer,
                            new ConfessionReminderFragment()
                    )
                    .addToBackStack(null)
                    .commit();
        });

        passwordProtectionCard.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.mainContainer,
                            new PasswordProtectionFragment()
                    )
                    .addToBackStack(null)
                    .commit();
        });

        checkUpdatesCard.setOnClickListener(v -> checkForUpdates());

        return view;
    }

    @Override
    public void onDestroyView() {
        updateStatus = null;
        checkUpdatesCard = null;
        super.onDestroyView();
    }

    private void checkForUpdates() {
        if (checkingForUpdates) {
            return;
        }
        checkingForUpdates = true;
        checkUpdatesCard.setEnabled(false);
        updateStatus.setText("Checking for updates…");

        Context appContext = requireContext().getApplicationContext();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppUpdateManager.UpdateResult result = null;
            String error = null;
            try {
                result = AppUpdateManager.checkForUpdate(appContext);
            } catch (IOException | IllegalArgumentException | IllegalStateException exception) {
                error = exception.getMessage();
            }

            AppUpdateManager.UpdateResult updateResult = result;
            String updateError = error;
            new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isAdded()) {
                        return;
                    }
                    checkingForUpdates = false;
                    if (checkUpdatesCard == null || updateStatus == null) {
                        return;
                    }
                    checkUpdatesCard.setEnabled(true);
                    if (updateError != null) {
                        updateStatus.setText(
                                updateError.isEmpty()
                                        ? "Could not check for updates. Please try again."
                                        : updateError
                        );
                    } else if (updateResult == null) {
                        updateStatus.setText("Could not check for updates. Please try again.");
                    } else if (!updateResult.hasUpdate()) {
                        updateStatus.setText("You're up to date (v" + updateResult.version + ").");
                    } else {
                        updateStatus.setText("Downloading version " + updateResult.version + "…");
                        prepareUpdateInstall(updateResult);
                    }
                });
            executor.shutdown();
        });
    }

    private void prepareUpdateInstall(AppUpdateManager.UpdateResult updateResult) {
        pendingUpdateApk = updateResult.apkFile;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && !requireContext().getPackageManager().canRequestPackageInstalls()) {
            updateStatus.setText("Allow installs from this app to install the update.");
            Intent settingsIntent = new Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + requireContext().getPackageName())
            );
            try {
                installPermissionLauncher.launch(settingsIntent);
            } catch (ActivityNotFoundException exception) {
                updateStatus.setText("This device does not support APK installation from Settings.");
            }
            return;
        }
        installUpdate(updateResult.apkFile);
    }

    private void installUpdate(File apkFile) {
        if (!apkFile.isFile()) {
            if (updateStatus != null) {
                updateStatus.setText("The downloaded update is missing. Please check again.");
            }
            return;
        }
        try {
            Uri apkUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".updateprovider",
                    apkFile
            );
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(installIntent);
        } catch (ActivityNotFoundException exception) {
            updateStatus.setText("No package installer is available on this device.");
        } catch (IllegalArgumentException exception) {
            updateStatus.setText("Unable to open the downloaded update.");
        }
    }

}
