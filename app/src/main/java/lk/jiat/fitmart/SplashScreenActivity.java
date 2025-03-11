package lk.jiat.fitmart;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

public class SplashScreenActivity extends AppCompatActivity {
    private ImageView logoImageView;
    private TextView fromTextView, enchantTextView;
    private ProgressBar progressBar;
    private static final int SPLASH_DELAY = 2000; // 2 seconds delay

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (isLoggedIn) {
                intent = new Intent(SplashScreenActivity.this, HomeActivity.class);
            } else {
                intent = new Intent(SplashScreenActivity.this, SignInActivity.class);
            }
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);


        logoImageView = findViewById(R.id.imageView8);
        fromTextView = findViewById(R.id.textView11);
        enchantTextView = findViewById(R.id.textView10);
        progressBar = findViewById(R.id.progressBar);


        startLogoSpringAnimation();
        startTextFlingAnimation();
        startProgressBarSpringAnimation();

    }


    private void startLogoSpringAnimation() {
        SpringAnimation springAnimation = new SpringAnimation(logoImageView, DynamicAnimation.TRANSLATION_Y);
        SpringForce springForce = new SpringForce();
        springForce.setStiffness(SpringForce.STIFFNESS_VERY_LOW);
        springForce.setFinalPosition(500f);
        springForce.setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);

        springAnimation.setSpring(springForce);
        springAnimation.start();
    }

    private void startTextFlingAnimation() {
        // Fling animation for "from" text (vertical movement)
        FlingAnimation flingAnimationFrom = new FlingAnimation(fromTextView, DynamicAnimation.TRANSLATION_Y);
        flingAnimationFrom.setStartVelocity(-2000f) // Initial velocity
                .setFriction(1.5f) // Friction to slow down
                .start();

        // Fling animation for "enchant" text (vertical movement)
        FlingAnimation flingAnimationEnchant = new FlingAnimation(enchantTextView, DynamicAnimation.TRANSLATION_Y);
        flingAnimationEnchant.setStartVelocity(-2000f)
                .setFriction(1.5f)
                .start();
    }

    private void startProgressBarSpringAnimation() {
        // Spring animation for progress bar's alpha (fade-in effect)
        SpringAnimation alphaAnimation = new SpringAnimation(progressBar, DynamicAnimation.ALPHA, 1f);
        SpringForce springForce = new SpringForce()
                .setFinalPosition(1f) // Fully visible
                .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY) // Less bouncy
                .setStiffness(SpringForce.STIFFNESS_MEDIUM);

        alphaAnimation.setSpring(springForce);
        alphaAnimation.start();
    }
}