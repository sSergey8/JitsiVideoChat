package com.example.jitsiapp.update;

import android.app.Activity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {  // Проверка версии

    public interface Callback {
        void onUpdateAvailable(JSONObject json);
        void onUpToDate();
        void onError();
    }

    private static final String VERSION_URL =
            "https://raw.githubusercontent.com/sSergey8/jitsi-update/main/version.json";

    public static void check(Activity activity, Callback callback) {
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

                int currentVersion =
                        activity.getPackageManager()
                                .getPackageInfo(activity.getPackageName(), 0)
                                .versionCode;

                activity.runOnUiThread(() -> {
                    if (remoteVersion > currentVersion) {
                        callback.onUpdateAvailable(obj);
                    } else {
                        callback.onUpToDate();
                    }
                });

            } catch (Exception e) {
                activity.runOnUiThread(callback::onError);
            }
        }).start();
    }
}
