package com.example.uts_a22202303001;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ambil session login dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.contains("email");

        new Handler().postDelayed(() -> {
            Intent intent;
            if (isLoggedIn) {
                // Jika user sudah login, langsung arahkan ke HomeFragment
                intent = new Intent(MainActivity.this, MainHome.class);
                intent.putExtra("fragmentToLoad", "home"); // agar langsung ke HomeFragment
            } else {
                // Jika belum login, arahkan ke halaman login
                intent = new Intent(MainActivity.this, MainLogin.class);
            }
            startActivity(intent);
            finish();
        }, 3000); // Delay splash 3 detik
    }
}
