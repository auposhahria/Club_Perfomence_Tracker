package com.example.clubperfomencetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView ivLogo = findViewById(R.id.ivLogo);
        TextView tvName = findViewById(R.id.tvCommunityName);

        // Professional entrance: Pop-in with bounce
        ivLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> {
                    // Slide up and fade in the community name
                    tvName.animate()
                            .alpha(1f)
                            .translationY(0)
                            .setDuration(600)
                            .withEndAction(() -> {
                                tvName.postDelayed(this::navigateToNext, 800);
                            })
                            .start();
                })
                .start();
    }

    private void navigateToNext() {
        Intent intent;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}