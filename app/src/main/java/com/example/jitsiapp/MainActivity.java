package com.example.jitsiapp;

import android.os.Bundle;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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
            "https://raw.githubusercontent.com/sSergey8/JitsiVideoChat/main/version.json";

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
                        // ПОКА просто лог
                        Toast.makeText(
                                this,
                                "Доступно обновление",
                                Toast.LENGTH_LONG
                        ).show();
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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkForUpdate();
    }


//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        try {
//            String displayName = "Guest-" + new Random().nextInt(10000);
//
//            URL serverURL = new URL("https://meet.jit.si");
//
//            JitsiMeetConferenceOptions options
//                    = new JitsiMeetConferenceOptions.Builder()
//                    .setServerURL(serverURL)
//                    .setRoom("my-test-room-123456")
//                    .setAudioMuted(false)
//                    .setVideoMuted(false)
//                    .setFeatureFlag("prejoinpage.enabled", false)
//                    .setFeatureFlag("welcomepage.enabled", false)
//                    .setFeatureFlag("call-integration.enabled", false)
//                    .build();
//
//            JitsiMeetActivity.launch(this, options);
//            finish();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
