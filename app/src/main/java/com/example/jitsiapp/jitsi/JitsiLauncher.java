package com.example.jitsiapp.jitsi;

import android.content.Context;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;

public class JitsiLauncher {  // Запуск видеоконференции
    public static void start(Context context) {
        try {
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

            JitsiMeetActivity.launch(context, options);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
