package com.example.jitsiapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;

import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


import android.os.Bundle;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.FileProvider;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // https://meet.jit.si/my-room-borovichi-2006

    private static final String VERSION_URL =
            "https://raw.githubusercontent.com/sSergey8/jitsi-update/main/version.json";


    private int getCurrentVersionCode() {
        try {
            return getPackageManager()
                    .getPackageInfo(getPackageName(), 0)
                    .versionCode;
        } catch (Exception e) {
            return 0;
        }
    }

    private void checkForUpdate() {
        new Thread(() -> {
            try {
                URL url = new URL(VERSION_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                reader.close();

                JSONObject obj = new JSONObject(json.toString());

                int remoteVersion = obj.getInt("versionCode");

                int currentVersion = getCurrentVersionCode();

                runOnUiThread(() -> {
                    if (remoteVersion > currentVersion) {
                        showUpdateDialog(obj);
                    } else {
                        startJitsi();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(this::startJitsi);
            }
        }).start();
    }

    private void startJitsi() {
        try {
            String displayName = "Guest-" + new Random().nextInt(10000);

            URL serverURL = new URL("https://meet.jit.si");

            JitsiMeetConferenceOptions options =
                    new JitsiMeetConferenceOptions.Builder()
                            .setServerURL(serverURL)
                            .setRoom("my-room-borovichi-2006")
                            .setAudioMuted(false)
                            .setVideoMuted(false)
                            .setFeatureFlag("prejoinpage.enabled", false)
                            .setFeatureFlag("welcomepage.enabled", false)
                            .setFeatureFlag("call-integration.enabled", false)
                            .build();

            JitsiMeetActivity.launch(this, options);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showUpdateDialog(JSONObject obj) {
        try {
            String versionName = obj.getString("versionName");
            String apkUrl = obj.getString("apkUrl");

            new AlertDialog.Builder(this)
                    .setTitle("Доступно обновление")
                    .setMessage("Доступна новая версия: " + versionName)
                    .setCancelable(false)
                    .setPositiveButton("Обновить", (dialog, which) -> {
                        openApkUrl(apkUrl);
                    })
                    .setNegativeButton("Позже", (dialog, which) -> {
                        startJitsi();
                    })
                    .show();

        } catch (Exception e) {
            startJitsi();
        }
    }

    private void openApkUrl(String apkUrl) {
        if (!canInstallApk()) {
            requestInstallPermission();
            return;
        }

        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(apkUrl);
                connection = (HttpURLConnection) url.openConnection();

                // ВАЖНО для GitHub
                connection.setRequestProperty("User-Agent", "Android");
                connection.setRequestProperty("Accept", "application/octet-stream");
                connection.setInstanceFollowRedirects(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int code = connection.getResponseCode();

                if (code != HttpURLConnection.HTTP_OK) {
                    throw new RuntimeException("HTTP error code: " + code);
                }

                InputStream input = connection.getInputStream();

                File apkFile = new File(
                        getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        "update.apk"
                );

                FileOutputStream output = new FileOutputStream(apkFile);

                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                runOnUiThread(() -> installApk(apkFile));

            } catch (Exception e) {
                e.printStackTrace();
                String msg = e.getMessage();

                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Ошибка загрузки APK: " + msg,
                                Toast.LENGTH_LONG
                        ).show()
                );
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private void installApk(File apkFile) {
        Uri apkUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                apkFile
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(intent);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkForUpdate();
    }
}
