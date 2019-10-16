package com.example.teambassador;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private TextView yetToUploadTextView, pendingTextView, rejectedTextView, completedTextView, statusTitleTextView, uploadTitleTextView, taskDescriptionTextView;
    private ProgressBar statusBar;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Button selectImageButton;
    private final int IMAGE_CODE = 101;
    private Uri imageUri;
    private String imageName;
    private ImageView uploadImageView;
    private AVLoadingIndicatorView progressBar;
    private LinearLayout linearLayout, linearLayout2;
    private CardView cardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        yetToUploadTextView = findViewById(R.id.yetToUploadTextView);
        pendingTextView = findViewById(R.id.pendingTextView);
        rejectedTextView = findViewById(R.id.rejectedTextView);
        completedTextView = findViewById(R.id.completedTextView);
        taskDescriptionTextView = findViewById(R.id.taskDescriptionTextView);
        statusTitleTextView = findViewById(R.id.statusTitleTextView);
        uploadTitleTextView = findViewById(R.id.uploadTitleTextView);
        statusBar = findViewById(R.id.statusBar);
        progressBar = findViewById(R.id.progressBar);
        selectImageButton = findViewById(R.id.selectImageButton);
        uploadImageView = findViewById(R.id.uploadImageView);
        linearLayout = findViewById(R.id.linearLayout);
        linearLayout2 = findViewById(R.id.linearLayout2);
        cardView = findViewById(R.id.cardView);

        pendingTextView.setVisibility(View.INVISIBLE);
        rejectedTextView.setVisibility(View.INVISIBLE);
        completedTextView.setVisibility(View.INVISIBLE);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference().child("Ishaan");

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("INFO","Select Image Button Clicked");

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_CODE);
            }
        });
    }

    private void uploadFile()
    {
        if(imageUri != null)
        {
            byte[] bytes = null;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                bitmap.compress(Bitmap.CompressFormat.JPEG,50,stream);
                bytes = stream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }

            imageName = "Ishaan" + "TaskName" + "." + getFileExtension(imageUri);

            StorageReference storageReference1 = storageReference.child(imageName);

            UploadTask uploadTask = storageReference1.putBytes(bytes);

            uploadTask
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(getApplicationContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
                            selectImageButton.setEnabled(false);
                            selectImageButton.setAlpha(0.2f);
                            statusBar.setProgress(2);
                            yetToUploadTextView.setVisibility(View.INVISIBLE);
                            pendingTextView.setVisibility(View.VISIBLE);
                            showUI();
//                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(getApplicationContext(), "Error occurred. Try Again", Toast.LENGTH_SHORT).show();
                            showUI();

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            hideUI();
                        }
                    });
        }
        else
        {
            Toast.makeText(getApplicationContext(),"No File Chosen",Toast.LENGTH_SHORT).show();
        }

    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public void hideUI()
    {
        Float alpha = 0.2f;
        linearLayout.setAlpha(alpha);
        linearLayout2.setAlpha(alpha);
        cardView.setAlpha(alpha);
        taskDescriptionTextView.setAlpha(alpha);
        statusTitleTextView.setAlpha(alpha);
        uploadTitleTextView.setAlpha(alpha);
        statusBar.setAlpha(alpha);
        cardView.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void showUI()
    {
        Float alpha = 1.0f;
        linearLayout.setAlpha(alpha);
        linearLayout2.setAlpha(alpha);
        cardView.setAlpha(alpha);
        taskDescriptionTextView.setAlpha(alpha);
        statusTitleTextView.setAlpha(alpha);
        uploadTitleTextView.setAlpha(alpha);
        statusBar.setAlpha(alpha);
        cardView.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if(requestCode == IMAGE_CODE)
            {
                imageUri = data.getData();
                Log.i("INFO","Getting Image");

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Upload Image?")
                        .setMessage("Are you sure you want to upload the selected image?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.i("INFO","YES Clicked");
                                hideUI();
                                Glide
                                        .with(getApplicationContext())
                                        .load(imageUri)
                                        .into(uploadImageView);
                                uploadFile();
                            }
                        })
                        .setNegativeButton("No",null).show();
            }
        }catch (Exception e){
            Toast.makeText(this, "Error getting image", Toast.LENGTH_SHORT).show();
        }
    }
}
