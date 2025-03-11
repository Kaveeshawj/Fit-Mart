package lk.jiat.fitmart;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import lk.jiat.fitmart.model.CustomToast;
import lk.jiat.fitmart.model.SQLiteHelper;

public class SignUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.button));


        SQLiteHelper sqLiteHelper = new SQLiteHelper(SignUp.this, "fitmart.db", null, 1);

        Spinner spinner = findViewById(R.id.speciality);

        String[] gender = new String[]{"Gender", "Male", "Female"};

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                SignUp.this,
                R.layout.gender_dropdown,
                gender
        );

        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        Button button = findViewById(R.id.button2);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(SignUp.this, SignInActivity.class);
            startActivity(intent);
        });

        Button button1 = findViewById(R.id.button);
        TextInputEditText fnameEditText = findViewById(R.id.fname);
        TextInputEditText lnameEditText = findViewById(R.id.lnamec);
        TextInputEditText mobileEditText = findViewById(R.id.mobile);
        TextInputEditText emailEditText = findViewById(R.id.experience);
        TextInputEditText passwordEditText = findViewById(R.id.fees);

        button1.setOnClickListener(v -> {

            String fname = fnameEditText.getText().toString().trim();
            String lname = lnameEditText.getText().toString().trim();
            String mobile = mobileEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String genderitem = spinner.getSelectedItem().toString();

            if (fname.isEmpty()) {
                CustomToast.show(SignUp.this, "Please enter the first name", R.drawable.warning);
            } else if (lname.isEmpty()) {
                CustomToast.show(SignUp.this, "Please enter the last name", R.drawable.warning);
            } else if (mobile.isEmpty()) {
                CustomToast.show(SignUp.this, "Please enter the mobile number", R.drawable.warning);
            } else if (mobile.length() != 10) {
                CustomToast.show(SignUp.this, "Invalid mobile number", R.drawable.warning);
            } else if (email.isEmpty()) {
                CustomToast.show(SignUp.this, "Please enter the email", R.drawable.warning);
            } else if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                CustomToast.show(SignUp.this, "Invalid Email!", R.drawable.warning);
            } else if (password.isEmpty()) {
                CustomToast.show(SignUp.this, "Please enter the password", R.drawable.warning);
            } else if (!password.matches("^(?=.*[a-zA-Z])(?=.*\\d).{6,}$")) {
                CustomToast.show(SignUp.this, "Password must be at least 6 characters long and include letters and numbers", R.drawable.warning);
            } else if (genderitem.equals("Gender")) {
                CustomToast.show(SignUp.this, "Please select a gender", R.drawable.warning);
            }  else {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("users")
                        .whereEqualTo("mobile", mobile)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (!task.getResult().isEmpty()) {
                                    CustomToast.show(SignUp.this, "Mobile number is already registered", R.drawable.warning);
                                } else {
                                    new Thread(() -> {
                                        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
                                        Cursor cursor = db.rawQuery("SELECT gender_id FROM gender WHERE gender = ?", new String[]{genderitem});
                                        if (cursor != null && cursor.moveToFirst()) {
                                            int genderId = cursor.getInt(cursor.getColumnIndexOrThrow("gender_id"));
                                            cursor.close();

                                            ContentValues values = new ContentValues();
                                            values.put("fname", fname);
                                            values.put("lname", lname);
                                            values.put("email", email);
                                            values.put("mobile", mobile);
                                            values.put("password", password);
                                            values.put("registeredDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                                            values.put("gender_id", genderId);

                                            long result = db.insert("user", null, values);
                                            runOnUiThread(() -> {
                                                if (result == -1) {
                                                    CustomToast.show(SignUp.this, "User registration failed", R.drawable.warning);
                                                } else {
                                                    HashMap<String, Object> user = new HashMap<>();
                                                    user.put("fname", fname);
                                                    user.put("lname", lname);
                                                    user.put("email", email);
                                                    user.put("mobile", mobile);
                                                    user.put("password", password);
                                                    user.put("gender", genderitem);
                                                    user.put("registeredDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

                                                    firestore.collection("users").add(user)
                                                            .addOnSuccessListener(documentReference -> {
                                                                CustomToast.show(SignUp.this, "User registered successfully", R.drawable.success);
                                                                Intent i = new Intent(SignUp.this, SignInActivity.class);
                                                                i.putExtra("name", fname);
                                                                startActivity(i);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                CustomToast.show(SignUp.this, "Error In Firebase!", R.drawable.error);
                                                            });
                                                }
                                            });
                                        } else {
                                            runOnUiThread(() -> {
                                                CustomToast.show(SignUp.this, "Invalid gender selection", R.drawable.warning);
                                            });
                                            if (cursor != null) {
                                                cursor.close();
                                            }
                                        }
                                    }).start();
                                }
                            } else {
                                // Handle Firestore query failure
                                CustomToast.show(SignUp.this, "Error checking mobile number", R.drawable.error);
                            }
                        });
            }
        });
    }
}