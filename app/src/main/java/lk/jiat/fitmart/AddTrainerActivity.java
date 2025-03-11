package lk.jiat.fitmart;

import static android.content.ContentValues.TAG;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lk.jiat.fitmart.model.CustomToast;
import lk.jiat.fitmart.model.SQLiteHelper;

public class AddTrainerActivity extends AppCompatActivity {

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ImageButton imageButton;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_trainer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        spinners();

        setupImagePicker();


        imageButton = findViewById(R.id.imageButton);

        imageButton.setOnClickListener(v -> pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        ));

        TextInputEditText fnameEditText = findViewById(R.id.fname);
        TextInputEditText lnameEditText = findViewById(R.id.lnamec);
        TextInputEditText mobileEditText = findViewById(R.id.mobile);
        TextInputEditText experEditText = findViewById(R.id.experience);
        TextInputEditText feesEditText = findViewById(R.id.fees);
        TextInputEditText line1EditText = findViewById(R.id.tline1);
        TextInputEditText line2EditText = findViewById(R.id.tline2);
        TextInputEditText cityEditText = findViewById(R.id.city);
        Spinner speialitySpinner = findViewById(R.id.speciality);
        Spinner genderspinner = findViewById(R.id.gender);

        Button button = findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fname = fnameEditText.getText().toString();
                String lname = lnameEditText.getText().toString();
                String mobile = mobileEditText.getText().toString();
                String experience = experEditText.getText().toString();
                String fee = feesEditText.getText().toString();
                String line1 = line1EditText.getText().toString();
                String line2 = line2EditText.getText().toString();
                String city = cityEditText.getText().toString();
                String gender = genderspinner.getSelectedItem().toString();
                String speiality = speialitySpinner.getSelectedItem().toString();

                if (fname.isEmpty()) {
                    CustomToast.show(AddTrainerActivity.this, "Please Enter the First Name", R.drawable.warning);

                } else if (lname.isEmpty()) {
                    CustomToast.show(AddTrainerActivity.this, "Please Enter the Last Name", R.drawable.warning);

                } else if (mobile.isEmpty()) {
                    CustomToast.show(AddTrainerActivity.this, "Please Enter the Mobile", R.drawable.warning);

                } else if (speiality.equals("Speciality")) {
                    CustomToast.show(AddTrainerActivity.this, "Please Select a Speciality", R.drawable.warning);

                } else if (experience.isEmpty()) {
                    CustomToast.show(AddTrainerActivity.this, "Please Enter the Experience", R.drawable.warning);

                } else if (fee.isEmpty()) {
                    CustomToast.show(AddTrainerActivity.this, "Please Enter the Fees", R.drawable.warning);

                } else if (gender.equals("Gender")) {
                    CustomToast.show(AddTrainerActivity.this, "Please Select a Gender", R.drawable.warning);

                } else if (line1.isEmpty()) {
                    CustomToast.show(AddTrainerActivity.this, "Please Enter the line1", R.drawable.warning);

                } else if (line2.isEmpty()) {
                    CustomToast.show(AddTrainerActivity.this, "Please Enter the line2", R.drawable.warning);

                } else if (city.isEmpty()) {
                    CustomToast.show(AddTrainerActivity.this, "Please Enter the city", R.drawable.warning);

                } else {

                    HashMap<String, Object> trainerMap = new HashMap<>();
                    trainerMap.put("fname", fname);
                    trainerMap.put("lname", lname);
                    trainerMap.put("mobile", mobile);
                    trainerMap.put("speiality", speiality);
                    trainerMap.put("experience", experience);
                    trainerMap.put("gender", gender);
                    trainerMap.put("fee", fee);
                    trainerMap.put("line1", line1);
                    trainerMap.put("line2", line2);
                    trainerMap.put("city", city);
                    trainerMap.put("status", "available");

                    uploadtrainerInfo(trainerMap);

                }
            }
        });

    }

    private void spinners() {

        Spinner specialitySpinner = findViewById(R.id.speciality);
        String[] specialityList = new String[]{"Speciality", "Strength", "Cardio",
                "Weight Loss", "Muscle Gain","Nutrition", "CrossFit"};

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                AddTrainerActivity.this,
                R.layout.gender_dropdown,
                specialityList
        );
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        specialitySpinner.setAdapter(arrayAdapter);

        Spinner genderSpinner = findViewById(R.id.gender);
        String[] genderNames = new String[]{"Gender", "Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                AddTrainerActivity.this,
                R.layout.gender_dropdown,
                genderNames
        );
        genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);
    }
    private void uploadtrainerInfo(HashMap<String, Object> trainerMap) {
        if (selectedImageUri == null) {
            saveToFirestore(trainerMap, null);
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("trainer_images")
                .child(System.currentTimeMillis() + ".jpg");

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl) {
                                trainerMap.put("imageUrl", downloadUrl.toString());
                                saveToFirestore(trainerMap, storageRef.getPath());

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CustomToast.show(AddTrainerActivity.this, "Image upload failed", R.drawable.error);
                    }
                });
    }

    private void saveToFirestore(HashMap<String, Object> trainerMap, String imagePath) {
        if (imagePath != null) {
            trainerMap.put("imagePath", imagePath);
        }

        FirebaseFirestore.getInstance().collection("trainer")
                .add(trainerMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        CustomToast.show(AddTrainerActivity.this, "Trainer Added Successfully!", R.drawable.success);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CustomToast.show(AddTrainerActivity.this, "Error Occurred", R.drawable.error);
                        if (imagePath != null) {
                            FirebaseStorage.getInstance().getReference(imagePath).delete();
                        }
                    }
                });
    }

    private void setupImagePicker() {
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                setCircularImage(uri);
            } else {
                Log.d(TAG, "No media selected");
            }
        });
    }

    private Bitmap cropAndResizeImage(Uri imageUri, int targetWidth, int targetHeight) {
        try {
            // Load the original bitmap
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            // Calculate scaling factor
            float scaleWidth = ((float) targetWidth) / originalBitmap.getWidth();
            float scaleHeight = ((float) targetHeight) / originalBitmap.getHeight();
            float scaleFactor = Math.max(scaleWidth, scaleHeight);

            // Create scaled bitmap
            Matrix matrix = new Matrix();
            matrix.postScale(scaleFactor, scaleFactor);

            Bitmap scaledBitmap = Bitmap.createBitmap(
                    originalBitmap,
                    0,
                    0,
                    originalBitmap.getWidth(),
                    originalBitmap.getHeight(),
                    matrix,
                    true
            );

            // Crop to exact target size
            Bitmap croppedBitmap = Bitmap.createBitmap(
                    scaledBitmap,
                    (scaledBitmap.getWidth() - targetWidth) / 2,
                    (scaledBitmap.getHeight() - targetHeight) / 2,
                    targetWidth,
                    targetHeight
            );

            return croppedBitmap;
        } catch (IOException e) {
            Log.e(TAG, "Error cropping image", e);
            return null;
        }
    }

    private void setCircularImage(Uri imageUri) {
        try {
            // Crop and resize to 300x300
            Bitmap croppedBitmap = cropAndResizeImage(imageUri, 300, 300);
            if (croppedBitmap != null) {
                Bitmap circularBitmap = getCircularBitmap(croppedBitmap);
                imageButton.setImageBitmap(circularBitmap);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load image", e);
        }
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        RectF rect = new RectF(0, 0, size, size);
        Path path = new Path();
        path.addOval(rect, Path.Direction.CCW);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, null, rect, paint);
        return output;

    }
}