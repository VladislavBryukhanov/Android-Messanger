package com.example.nameless.autoupdating.common;

import android.content.Context;
import android.widget.LinearLayout;

import com.example.nameless.autoupdating.R;

public class AudioTrackUI extends LinearLayout {

    public AudioTrackUI(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.audio_track_ui, this);
    }
}
