package com.example.uts_a22202303001.ui.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.uts_a22202303001.MainHistory;
import com.example.uts_a22202303001.MainLogin;
import com.example.uts_a22202303001.R;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        boolean isGuest = sharedPreferences.getBoolean("is_guest", false);

        if (isGuest) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Akses Ditolak")
                    .setMessage("Silakan login terlebih dahulu untuk mengakses profil.")
                    .setCancelable(false)
                    .setPositiveButton("Login", (dialog, which) -> {
                        Intent intent = new Intent(requireActivity(), MainLogin.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .show();
            return view; // Tetap return view agar fragment tidak crash
        }

        Button btnEdit = view.findViewById(R.id.btn_edit_profile);
        Button btnKontak = view.findViewById(R.id.btn_kontak_kami);
        Button btnHistory = view.findViewById(R.id.btn_order_histori);
        Button btnChangePassword = view.findViewById(R.id.btn_change_password);

        btnEdit.setOnClickListener(v -> {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main)
                    .navigate(R.id.editProfileFragment);
        });

        btnChangePassword.setOnClickListener(v -> {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main)
                    .navigate(R.id.ChangePasswordFragment);
        });

        btnKontak.setOnClickListener(v -> {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main)
                    .navigate(R.id.kontakKamiFragment); // arahkan ke fragment kontak
        });

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainHistory.class);
            startActivity(intent);
});

        return view;
    }
}
