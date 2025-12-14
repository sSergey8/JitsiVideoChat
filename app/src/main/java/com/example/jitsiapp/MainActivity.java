package com.example.jitsiapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // https://meet.jit.si/my-test-room-123456

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            String displayName = "Guest-" + new Random().nextInt(10000);

            URL serverURL = new URL("https://meet.jit.si");

            JitsiMeetConferenceOptions options
                    = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(serverURL)
                    .setRoom("my-test-room-123456")
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
}
