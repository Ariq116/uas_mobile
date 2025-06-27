package com.example.uts_a22202303001.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.uts_a22202303001.R;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private TextView tvGreeting;
    private ImageSlider imageSlider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inisialisasi View
        tvGreeting = view.findViewById(R.id.tvGreeting);
        imageSlider = view.findViewById(R.id.imageSlider);

        // Ambil nama user dari SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String nama = sharedPreferences.getString("nama", "User");

        // Tampilkan sapaan
        tvGreeting.setText("Selamat Datang " + nama + " 🙌🏻");

        // Setup Image Slider
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.bannersepatu1, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.bannersepatu2, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.bannersepatu3, ScaleTypes.FIT));


        imageSlider.setImageList(slideModels, ScaleTypes.FIT);

        return view;
    }
}
