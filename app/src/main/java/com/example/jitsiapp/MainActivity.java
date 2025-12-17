package com.example.jitsiapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jitsiapp.jitsi.JitsiLauncher;
import com.example.jitsiapp.update.ApkDownloader;
import com.example.jitsiapp.update.ApkInstaller;
import com.example.jitsiapp.update.UpdateChecker;
import org.json.JSONObject;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    // https://meet.jit.si/my-room-borovichi-2006
    private volatile boolean isUpdating = false;
    private String pendingApkUrl = null;

    private void showUpdateDialog(JSONObject obj) {
        try {
            String versionName = obj.getString("versionName");
            String apkUrl = obj.getString("apkUrl");

            new AlertDialog.Builder(this)
                    .setTitle("Доступно обновление")
                    .setMessage("Доступна новая версия: " + versionName)
                    .setCancelable(false)
                    .setPositiveButton("Обновить", (dialog, which) -> {
                        if (isUpdating) return;
                        isUpdating = true;
                        startUpdate(apkUrl);
                    })
                    .setNegativeButton("Позже", (dialog, which) -> {
                        JitsiLauncher.start(this);
                        finish();
                    })
                    .show();

        } catch (Exception e) {
            JitsiLauncher.start(this);
            finish();
        }
    }
    private void startUpdate(String apkUrl) {
        if (!canInstallApk()) {
            pendingApkUrl = apkUrl;
            requestInstallPermission();
            return;
        }

        setContentView(R.layout.activity_update);

        ApkDownloader.download(this, apkUrl, new ApkDownloader.Callback() {
            @Override
            public void onSuccess(File apkFile) {
                ApkInstaller.install(MainActivity.this, apkFile);
            }

            @Override
            public void onError(String message) {
                isUpdating = false;
                Toast.makeText(
                        MainActivity.this,
                        "Ошибка загрузки APK: " + message,
                        Toast.LENGTH_LONG
                ).show();

                JitsiLauncher.start(MainActivity.this);
                finish();
            }
        });
    }
    private boolean canInstallApk() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }
    private void requestInstallPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Intent intent = new Intent(
                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + getPackageName())
            );
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (pendingApkUrl != null && canInstallApk()) {
            String apkUrl = pendingApkUrl;
            pendingApkUrl = null;

            startUpdate(apkUrl);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UpdateChecker.check(this, new UpdateChecker.Callback() {
            @Override
            public void onUpdateAvailable(JSONObject json) {
                showUpdateDialog(json);
            }
            @Override
            public void onUpToDate() {
                JitsiLauncher.start(MainActivity.this);
                finish();
            }
            @Override
            public void onError() {
                JitsiLauncher.start(MainActivity.this);
                finish();
            }
        });
    }

}
