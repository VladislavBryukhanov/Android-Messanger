package com.example.nameless.autoupdating.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.example.nameless.autoupdating.asyncTasks.DownloadAvatarByUrl;
import com.example.nameless.autoupdating.R;
import com.example.nameless.autoupdating.common.AuthGuard;
import com.example.nameless.autoupdating.common.FirebaseSingleton;
import com.example.nameless.autoupdating.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AuthGuard {

    public static final String APP_PREFERENCES = "preferences";
    public static final String IS_NOTIFY_ENABLED = "IS_NOTIFY_ENABLED";
    public static final String IS_LOCATION_ENABLED  = "IS_LOCATION_ENABLED";
    public static final String STORAGE_MODE  = "STORAGE_MODE";
    public static final String CACHE_STORAGE  = "Cache storage";
    public static final String LOCAL_STORAGE  = "Local storage";
    private SharedPreferences settings;

    private User myProfile;
    private CircleImageView avatar;
    private EditText etLogin, etNickname, etBio;
    private Switch cbLocation, cbNotify;
    private MenuItem mSave;
    private boolean isNewAvatar = false;
    private Uri avatarImage;

    private Spinner spStorage;
    private ArrayList<String> storageModeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("");
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        avatar = findViewById(R.id.profile_image);
        etLogin = findViewById(R.id.etLogin);
        etNickname = findViewById(R.id.etNickname);
        etBio = findViewById(R.id.etBio);
        cbLocation = findViewById(R.id.cbLocation);
        cbNotify = findViewById(R.id.cbNotify);
        spStorage = findViewById(R.id.spStorage);

        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        myProfile = getMyAccount();

        /*boolean test = settings.contains(IS_NOTIFY_ENABLED);
        String tt = settings.getString(IS_NOTIFY_ENABLED, "");
        if(settings.contains(IS_NOTIFY_ENABLED)) {
            Toast.makeText(this, settings.getString(IS_NOTIFY_ENABLED, ""), Toast.LENGTH_SHORT).show();
        }*/
        cbNotify.setChecked(settings.getBoolean(IS_NOTIFY_ENABLED, false));
        cbLocation.setChecked(settings.getBoolean(IS_LOCATION_ENABLED, false));

        etLogin.setText(myProfile.getLogin());
        etNickname.setText(myProfile.getNickname());
        etBio.setText(myProfile.getBio());

        storageModeList = new ArrayList<>();
        storageModeList.add(CACHE_STORAGE);
        storageModeList.add(LOCAL_STORAGE);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_options_item,
                storageModeList);
        spStorage.setAdapter(adapter);

        String selectedValue = settings.getString(STORAGE_MODE, CACHE_STORAGE);
        int spinnerPosition = adapter.getPosition(selectedValue);
        spStorage.setSelection(spinnerPosition);

        spStorage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!spStorage.getSelectedItem().toString().equals(settings.getString(STORAGE_MODE, CACHE_STORAGE)) &&
                        spStorage.getSelectedItem().toString().equals(LOCAL_STORAGE)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            new ContextThemeWrapper(
                                    Settings.this,
                                    R.style.AppTheme));
                    builder.setTitle("Storage will be changed");
                    builder.setMessage("Your files will be saved in AUMessanger directory at root of storage");
                    builder.setPositiveButton("Ok", (dialogInterface, i1) -> dialogInterface.cancel());
                    AlertDialog dialog = builder.create();
                    dialog.setCancelable(true);
                    dialog.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        if(myProfile.getAvatar() != null) {
            avatar.setImageBitmap(BitmapFactory.decodeFile(myProfile.getAvatar()));
        }

        avatar.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
//            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(
                    intent, "Select Picture"), Chat.PICKFILE_RESULT_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == Chat.PICKFILE_RESULT_CODE) {
                isNewAvatar = true;
                avatarImage = data.getData();
/*                try {
                    myProfile.setAvatar(MediaStore.Images.Media.getBitmap(this.getContentResolver(), avatarImage));
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

//                myProfile.setAvatar(avatarImage);
                avatar.setImageURI(avatarImage);
            }
        }

    }

    private void setNewAvatar(final DatabaseReference myRef, final DataSnapshot data) {
        mSave.setVisible(false);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference gsReference;
        if(myProfile.getAvatarUrl() != null) {
            gsReference = storage.getReferenceFromUrl(myProfile.getAvatarUrl());
            gsReference.delete(); // Remove old avatar
        }

        String extension = getContentResolver().getType(avatarImage);
        extension = "." + extension.split("/")[1];

        gsReference = storage.getReferenceFromUrl("gs://messager-d15a0.appspot.com/");
        StorageReference riversRef = gsReference.child(myProfile
                .getEmail() + "/Avatar/" + java.util.UUID.randomUUID() + extension);
        UploadTask uploadTask = riversRef.putFile(avatarImage);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            myProfile.setAvatarUrl(taskSnapshot.getDownloadUrl().toString());
            myRef.child(data.getKey()).child("avatarUrl").setValue(myProfile.getAvatarUrl());

            DownloadAvatarByUrl downloadTask = new DownloadAvatarByUrl(avatar, myProfile, getApplication());
            try {
                downloadTask.execute(myProfile.getAvatarUrl()).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Settings.this.finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        mSave = menu.findItem(R.id.mSave);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.mSave: {
                Pattern pattern = Pattern.compile("[!@#$%^&*()-]");
                Matcher matcher = pattern.matcher(etNickname.getText().toString());
                if(matcher.find()) {
                    Toast.makeText(this, "Sorry, this nickname is invalid", Toast.LENGTH_SHORT).show();
                    break;
                }

                final FirebaseDatabase database = FirebaseSingleton.getFirebaseInstanse();
                final DatabaseReference myRef = database.getReference("Users");

                Query getNickname = database.getReference("Users").orderByChild("nickname").equalTo(etNickname.getText().toString());
                getNickname.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean isMyNickname = false;
                        if(dataSnapshot.exists()) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                String currentUid = (String) (data.child("uid").getValue());
                                if (currentUid != null && currentUid.equals(myProfile.getUid())) {
                                    isMyNickname = true;
                                }
                            }
                        }
                        if(!dataSnapshot.exists() || isMyNickname) {
                            Query getUser = database.getReference("Users").orderByChild("uid").equalTo(myProfile.getUid());
                            getUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot data : dataSnapshot.getChildren()) {
                                        myProfile.setBio(etBio.getText().toString());
                                        myProfile.setLogin(etLogin.getText().toString());
                                        myProfile.setNickname(etNickname.getText().toString());

                                        myRef.child(data.getKey()).child("login").setValue(myProfile.getLogin());
                                        myRef.child(data.getKey()).child("bio").setValue(myProfile.getBio());
                                        myRef.child(data.getKey()).child("nickname").setValue(myProfile.getNickname());

                                        SharedPreferences.Editor prefs = settings.edit();
                                        prefs.putBoolean(IS_NOTIFY_ENABLED, cbNotify.isChecked());
                                        prefs.putBoolean(IS_LOCATION_ENABLED, cbLocation.isChecked());
                                        prefs.putString(STORAGE_MODE, spStorage.getSelectedItem().toString());
                                        prefs.apply();

                                        if(isNewAvatar) {
                                            setNewAvatar(myRef, data);
                                        } else {
                                            Settings.this.finish();
                                        }

                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            });
                        } else {
                            Toast.makeText(Settings.this, "This nickname already exists", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });


                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
