package com.example.jitsiapp.update;

import android.app.Activity;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApkDownloader {  // Загрузка APK

    public interface Callback {
        void onSuccess(File apkFile);
        void onError(String message);
    }

    public static void download(Activity activity, String apkUrl, Callback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(apkUrl);
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestProperty("User-Agent", "Android");
                connection.setRequestProperty("Accept", "application/octet-stream");
                connection.setInstanceFollowRedirects(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new RuntimeException("HTTP " + connection.getResponseCode());
                }

                InputStream input = connection.getInputStream();

                File apkFile = new File(
                        activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
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

                activity.runOnUiThread(() -> callback.onSuccess(apkFile));

            } catch (Exception e) {
                String msg = e.getMessage();
                activity.runOnUiThread(() -> callback.onError(msg));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }
}
