package com.example.uts_a22202303001;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.uts_a22202303001.databinding.ActivityMainHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainHome extends AppCompatActivity {

    private ActivityMainHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Atur Toolbar sebagai ActionBar
        Toolbar toolbar = binding.myToolbar;
        setSupportActionBar(toolbar);

        // Tambahkan semua ID menu, termasuk navigation_home
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_product,
                R.id.navigation_order,
                R.id.navigation_profile
        ).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Cek apakah ada permintaan untuk langsung buka HomeFragment
        String fragmentToLoad = getIntent().getStringExtra("fragmentToLoad");
        if ("home".equals(fragmentToLoad)) {
            BottomNavigationView bottomNavigationView = binding.navView;
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return NavigationUI.navigateUp(navController, new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_product,
                R.id.navigation_order,
                R.id.navigation_profile
        ).build()) || super.onSupportNavigateUp();
    }
}
