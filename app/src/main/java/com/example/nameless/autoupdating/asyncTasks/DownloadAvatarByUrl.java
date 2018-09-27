package com.example.nameless.autoupdating.asyncTasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.nameless.autoupdating.R;
import com.example.nameless.autoupdating.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nameless on 13.06.18.
 */

public class DownloadAvatarByUrl extends AsyncTask<String, Void, Bitmap> {

    private ImageView bmImage;
    private User user;
    private Context parentContext;
    private Bitmap placeholder;

    public DownloadAvatarByUrl(ImageView bmImage, User user, Context parentContext) {
        this.bmImage = bmImage;
        this.user = user;
        this.parentContext = parentContext;
    }

    public DownloadAvatarByUrl(ImageView bmImage, User user, Context parentContext, Bitmap placeholder) {
        this.bmImage = bmImage;
        this.user = user;
        this.parentContext = parentContext;
        this.placeholder = placeholder;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
/*        InputStream in =null;
        try {
            in = new java.net.URL(strings[0]).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bmp = BitmapFactory.decodeStream(in);*/
//        Bitmap bmp = BitmapFactory.decodeFile(downloadFileByUrl(strings[0]));
//        return bmp;

        Bitmap  bmp = placeholder;
        String imagePath = downloadFileByUrl(strings[0]);
        if (imagePath != null) {
            bmp = BitmapFactory.decodeFile(imagePath);
        }
        return bmp;
    }

    @Override
    protected void onPostExecute(Bitmap bmp) {
        bmImage.setImageBitmap(bmp);
    }

    public String downloadFileByUrl(final String url) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference fileReference = storage.getReferenceFromUrl(url);

//        File path = new File(Environment.getExternalStorageDirectory()
//                + "/AUMessanger/Users/");
//        if(!path.exists()) {
//            path.mkdirs();
//        }

        final File imgFile = new File(parentContext.getCacheDir(), fileReference.getName());

        if (!imgFile.exists()) {
            fileReference.getFile(imgFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bmp = BitmapFactory.decodeFile(imgFile.getPath());
                    user.setAvatar(imgFile.getPath());
                     onPostExecute(bmp);
                }
            });
        } else {
            user.setAvatar(imgFile.getPath());
            return imgFile.getPath();
        }
    return null;
    }
}
