package lk.jiat.fitmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lk.jiat.fitmart.model.CustomToast;

public class UserProfileActivity extends AppCompatActivity implements SensorEventListener {
    private boolean isFlipped = false;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isToastShown = false;

    private static final String TAG = "UserProfileActivity";
    private ImageButton imageButton;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private String documentId;
    private String mobileUser;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        initializeViews();
        setupWindowInsets();
        setupImagePicker();
        loadUserData();
        setupUpdateButton();


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        Button button = findViewById(R.id.button4);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFlipped) {
                    logoutUser();
                } else {
                    CustomToast.show(UserProfileActivity.this, "Flip the device to confirm logout", R.drawable.warning);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float z = event.values[2];

            Log.d(TAG, "Accelerometer z-value: " + z);

            if (z > 9.0f) {
                Log.d(TAG, "Device is face-up");
                isFlipped = false;
                isToastShown = false;
            } else if (z < -9.0f) {
                Log.d(TAG, "Device is face-down");
                isFlipped = true;

                if (!isToastShown) {
                    runOnUiThread(() -> CustomToast.show(this, "Device flipped! Click logout again.", R.drawable.success));
                    isToastShown = true; // Mark the toast as shown
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void logoutUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .whereEqualTo("mobile", mobileUser)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    CustomToast.show(UserProfileActivity.this, "Logout successfully", R.drawable.success);

                                    isFlipped = false;
                                    isToastShown = false;
                                })
                                .addOnFailureListener(e -> {
                                    CustomToast.show(UserProfileActivity.this, "Failed to delete user data", R.drawable.error);

                                    isFlipped = false;
                                    isToastShown = false;

                                });
                    }
                })
                .addOnFailureListener(e -> {
                    CustomToast.show(UserProfileActivity.this, "Logout Failed!", R.drawable.error);
                });

        Intent intent = new Intent(UserProfileActivity.this, SplashScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void initializeViews() {
        imageButton = findViewById(R.id.imageButton);
        firestore = FirebaseFirestore.getInstance();

        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.button));
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupImagePicker() {
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d(TAG, "Selected URI: " + uri);
                setCircularImage(uri);
            } else {
                Log.d(TAG, "No media selected");
            }
        });

        imageButton.setOnClickListener(v -> pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        ));
    }

    private void loadUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        String mobile = sharedPreferences.getString("mobile", null);

        if (mobile != null) {
            firestore.collection("users")
                    .whereEqualTo("mobile", mobile)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            documentId = document.getId();
                            populateUserData(document);
                            loadProfileImage();
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error loading user data", e));
        }
    }

    private void populateUserData(DocumentSnapshot document) {
        Map<String, Object> userData = document.getData();
        if (userData != null) {
            setEditTextValue(R.id.fname, userData.get("fname"));
            setEditTextValue(R.id.lnamec, userData.get("lname"));
            setEditTextValue(R.id.mobile, userData.get("mobile"));
            setEditTextValue(R.id.experience, userData.get("email"));
            setEditTextValue(R.id.fees, userData.get("password"));
            setEditTextValue(R.id.tline1, userData.get("line1"));
            setEditTextValue(R.id.tline2, userData.get("line2"));
            setEditTextValue(R.id.city, userData.get("city"));
            setEditTextValue(R.id.postalcodetextview, userData.get("postal"));

            mobileUser = (String) userData.get("mobile");
        }
    }

    private void setEditTextValue(int viewId, Object value) {
        TextInputEditText editText = findViewById(viewId);
        editText.setText(value != null ? value.toString() : "");
    }

    private void setupUpdateButton() {
        Button updateButton = findViewById(R.id.button3);
        updateButton.setOnClickListener(v -> validateAndUpdateProfile());
    }

    private void validateAndUpdateProfile() {
        Map<String, String> fieldValues = new HashMap<>();
        fieldValues.put("fname", getEditTextValue(R.id.fname));
        fieldValues.put("lname", getEditTextValue(R.id.lnamec));
        fieldValues.put("email", getEditTextValue(R.id.experience));
        fieldValues.put("line1", getEditTextValue(R.id.tline1));
        fieldValues.put("line2", getEditTextValue(R.id.tline2));
        fieldValues.put("city", getEditTextValue(R.id.city));
        fieldValues.put("postal", getEditTextValue(R.id.postalcodetextview));

        if (validateFields(fieldValues)) {
            updateProfile(fieldValues);
        }
    }

    private String getEditTextValue(int viewId) {
        TextInputEditText editText = findViewById(viewId);
        return editText.getText().toString().trim();
    }

    private boolean validateFields(Map<String, String> fields) {
        if (fields.get("fname").isEmpty()) {
            showError("Please enter the first name");
            return false;
        }
        if (fields.get("lname").isEmpty()) {
            showError("Please enter the last name");
            return false;
        }
        if (fields.get("email").isEmpty()) {
            showError("Please enter the email");
            return false;
        }
        if (!fields.get("email").matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            showError("Invalid Email!");
            return false;
        }
        if (fields.get("line1").isEmpty()) {
            showError("Please enter Line 1 of the Address");
            return false;
        }
        if (fields.get("line2").isEmpty()) {
            showError("Please enter Line 2 of the Address");
            return false;
        }
        if (fields.get("city").isEmpty()) {
            showError("Please enter the city");
            return false;
        }
        if (fields.get("postal").isEmpty()) {
            showError("Please enter the postal Code");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        CustomToast.show(this, message, R.drawable.warning);
    }

    private void updateProfile(Map<String, String> fields) {
        if (documentId != null) {
            firestore.collection("users").document(documentId)
                    .update(new HashMap<>(fields))
                    .addOnSuccessListener(unused ->
                            CustomToast.show(this, "User profile updated successfully", R.drawable.success))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update profile", e);
                        CustomToast.show(this, "Failed to update profile", R.drawable.error);
                    });
        } else {
            Log.e(TAG, "Document ID is null. Cannot update Firestore.");
        }
    }

    private void setCircularImage(Uri imageUri) {
        try {
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            int size = 200;
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, size, size, true);
            Bitmap circularBitmap = getCircularBitmap(resizedBitmap);
            imageButton.setImageBitmap(circularBitmap);
            uploadImageToFirebase(imageUri);
        } catch (IOException e) {
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

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null && documentId != null) {
            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference()
                    .child("profile_images/" + documentId + ".jpg");

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            storageRef.getDownloadUrl()
                                    .addOnSuccessListener(uri -> saveImageUrlToFirestore(uri.toString()))
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to get download URL", e)))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to upload image", e));
        }
    }

    private void loadProfileImage() {
        if (documentId != null) {
            firestore.collection("users").document(documentId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("profileImage")) {
                            String imageUrl = documentSnapshot.getString("profileImage");
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(imageUrl)
                                        .into(imageButton);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to load profile image", e));
        }
    }

    private void saveImageUrlToFirestore(String imageUrl) {
        if (documentId != null) {
            firestore.collection("users").document(documentId)
                    .update("profileImage", imageUrl)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Image URL saved successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to save image URL", e));
        }
    }
}