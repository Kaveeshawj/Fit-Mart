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

import com.google.firebase.firestore.FieldValue;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import lk.jiat.fitmart.model.CustomToast;
import lk.jiat.fitmart.model.SQLiteHelper;

public class AddProductsActivity extends AppCompatActivity {

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ImageButton imageButton;
    private Uri selectedImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_products);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupImagePicker();

        categoryAdapter();

        FirebaseFirestore firestore=FirebaseFirestore.getInstance();

        TextInputEditText titleText = findViewById(R.id.fnamec);
        TextInputEditText descText = findViewById(R.id.lnamec);
        TextInputEditText priceText = findViewById(R.id.experience);
        TextInputEditText qtyText = findViewById(R.id.fees);
        TextInputEditText deliveryText = findViewById(R.id.tline1);
        Spinner spinner = findViewById(R.id.speciality);
        imageButton = findViewById(R.id.imageButton);

        imageButton.setOnClickListener(v -> pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        ));

        Button button = findViewById(R.id.button3);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleText.getText().toString();
                String des = descText.getText().toString();
                String price = priceText.getText().toString();
                String qty = qtyText.getText().toString();
                String delivery = deliveryText.getText().toString();
                String category = spinner.getSelectedItem().toString();

                if (title.isEmpty()) {
                    CustomToast.show(AddProductsActivity.this, "Please Enter the Tile", R.drawable.warning);
                } else if (des.isEmpty()) {
                    CustomToast.show(AddProductsActivity.this, "Please Enter the Description", R.drawable.warning);
                } else if (category.equals("Category")) {
                    CustomToast.show(AddProductsActivity.this, "Please Select a Category", R.drawable.warning);
                } else if (price.isEmpty()) {
                    CustomToast.show(AddProductsActivity.this, "Please Enter the Price", R.drawable.warning);
                } else if (qty.isEmpty()) {
                    CustomToast.show(AddProductsActivity.this, "Please Enter the Quantity", R.drawable.warning);
                } else if (delivery.isEmpty()) {
                    CustomToast.show(AddProductsActivity.this, "Please Enter the Delivery fee", R.drawable.warning);
                } else {

                    HashMap<String, Object> productMap = new HashMap<>();
                    productMap.put("title", title);
                    productMap.put("description", des);
                    productMap.put("category", category);
                    productMap.put("price", price);
                    productMap.put("qty", qty);
                    productMap.put("deliveryfee", delivery);
                    productMap.put("datetime", FieldValue.serverTimestamp());

                    uploadImageAndProduct(productMap);

                }


            }
        });
    }


    private void uploadImageAndProduct(HashMap<String, Object> productMap) {
        if (selectedImageUri == null) {
            saveProductToFirestore(productMap, null);
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("product_images")
                .child(System.currentTimeMillis() + ".jpg");

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl) {
                                productMap.put("imageUrl", downloadUrl.toString());
                                saveProductToFirestore(productMap, storageRef.getPath());

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CustomToast.show(AddProductsActivity.this, "Image upload failed", R.drawable.error);
                    }
                });
    }

    private void saveProductToFirestore(HashMap<String, Object> productMap, String imagePath) {
        if (imagePath != null) {
            productMap.put("imagePath", imagePath);
        }

        FirebaseFirestore.getInstance().collection("product")
                .add(productMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        CustomToast.show(AddProductsActivity.this, "Product Added Successfully!", R.drawable.success);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CustomToast.show(AddProductsActivity.this, "Error Occurred", R.drawable.error);
                        if (imagePath != null) {
                            FirebaseStorage.getInstance().getReference(imagePath).delete();
                        }
                    }
                });
    }
    private void categoryAdapter() {
        SQLiteHelper sqLiteHelper = new SQLiteHelper(AddProductsActivity.this, "fitmart.db", null, 1);
        SQLiteDatabase sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        List<String> categoryList = new ArrayList<>();

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM `category`", null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String category = cursor.getString(1);  // Verify column index
                categoryList.add(category);
            }
            cursor.close();
        } else {
            Log.e("FitMartLog", "No categories found or cursor is null");
        }

        categoryList.add(0, "Category");

        Spinner spinner = findViewById(R.id.speciality);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                AddProductsActivity.this,
                R.layout.gender_dropdown,
                categoryList
        );
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

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


