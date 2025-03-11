package lk.jiat.fitmart;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import lk.jiat.fitmart.model.CustomToast;

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.button));

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        TextInputEditText mobileEditText = findViewById(R.id.mobile);
        TextInputEditText passwordEditText = findViewById(R.id.fees);
        Button signInButton = findViewById(R.id.button);
        Button signUpButton = findViewById(R.id.button2);

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUp.class);
            startActivity(intent);
        });

        signInButton.setOnClickListener(v -> {
            String mobile = mobileEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (mobile.isEmpty()) {
                CustomToast.show(SignInActivity.this, "Please enter the mobile number", R.drawable.warning);
            } else if (password.isEmpty()) {
                CustomToast.show(SignInActivity.this, "Please enter the password", R.drawable.warning);
            } else {
                // Query Firestore for the user
                firestore.collection("users")
                        .whereEqualTo("mobile", mobile)
                        .whereEqualTo("password", password)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String name = document.getString("fname");
                                    String email = document.getString("email");

                                    SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("isLoggedIn", true);
                                    editor.putString("mobile", mobile);
                                    editor.putString("name", name);
                                    editor.putString("email", email);
                                    editor.apply();

                                    CustomToast.show(SignInActivity.this, "Sign-in successful", R.drawable.success);
                                    Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                shakeAnimation(mobileEditText);
                                shakeAnimation(passwordEditText);
                                mobileEditText.requestFocus();
                                mobileEditText.setText("");
                                passwordEditText.setText("");
                                CustomToast.show(SignInActivity.this, "Incorrect Mobile or Password", R.drawable.error);
                            }
                        });
            }
        });
    }

    private void shakeAnimation(TextInputEditText field) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(field, "translationX", 0f, 20f, -20f, 20f, -20f, 0f);
        shake.setDuration(500);
        shake.start();
    }
}