package com.example.nameless.autoupdating.common.MediaFileUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.example.nameless.autoupdating.R;

import java.io.File;

public class MediaFileHandler extends AsyncTask<File, Void, Bitmap> {

    private Context parentContext;
    private String fileType;

    private AudioComponent audioComponent;
    private ImageComponent imageComponent;
    private VideoComponent videoComponent;

    MediaFileHandler(
            ImageComponent imageComponent,
            Context parentContext,
            String fileType) {

        this.imageComponent = imageComponent;
        this.parentContext = parentContext;
        this.fileType = fileType;
    }

    MediaFileHandler(
            AudioComponent audioComponent,
            Context parentContext,
            String fileType) {

        this.audioComponent = audioComponent;
        this.parentContext = parentContext;
        this.fileType = fileType;
    }

    MediaFileHandler(
            VideoComponent videoComponent,
            Context parentContext,
            String fileType) {

        this.videoComponent = videoComponent;
        this.parentContext = parentContext;
        this.fileType = fileType;
    }

    @Override
    protected Bitmap doInBackground(File... file) {
        switch (fileType) {
            case MediaFileDownloader.IMAGE_TYPE: {
                return imageComponent.setImageProperties();
            }
            case MediaFileDownloader.AUDIO_TYPE: {
                return audioComponent.setAudioProperties();
            }
            case MediaFileDownloader.VIDEO_TYPE: {
                return videoComponent.setVideoProperties();
            }
            default: {
                return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                        parentContext.getResources(),
                        R.drawable.file), 100, 100, true);
            }
        }
    }

    @Override
    protected void onPostExecute(Bitmap bmp) {
        if(bmp != null) {
            switch (fileType) {
                case MediaFileDownloader.IMAGE_TYPE: {
                    imageComponent.setImageUi(bmp);
                    break;
                }
                case MediaFileDownloader.AUDIO_TYPE: {
                    audioComponent.setAudioUI(bmp);
                    break;
                }
                case MediaFileDownloader.VIDEO_TYPE: {
                    videoComponent.setVideoUi(bmp);
                    break;
                }
                default: {
                    imageComponent.setImageUi(bmp);
                }
            }
        }
    }
}
