package com.example.uts_a22202303001.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.uts_a22202303001.ApiClient;

import com.example.uts_a22202303001.databinding.FragmentChangePasswordBinding;
import com.example.uts_a22202303001.RegisterAPI;
import com.example.uts_a22202303001.ServerAPI;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordFragment extends Fragment {
    private FragmentChangePasswordBinding binding;

    private RegisterAPI api;
    private SharedPreferences sharedPreferences;
    private String email;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        sharedPreferences = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");
        api = ApiClient.getClient(ServerAPI.BASE_URL).create(RegisterAPI.class);
        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.btnSubmit.setOnClickListener(v -> changePassword(email));
        return view;
    }

    private void changePassword(String email) {
        String oldPassword = binding.etOldPassword.getText().toString().trim();
        String newPassword = binding.etNewPassword.getText().toString().trim();
        String confirmNewPassword = binding.etConfirmPassword.getText().toString().trim();

        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(getContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show();
        }

        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(getContext(), "Konfirmasi password tidak sesuai", Toast.LENGTH_SHORT).show();
        }

        Call<ResponseBody> call = api.changePassword(email, oldPassword, newPassword);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String result = response.body().string();
                        JSONObject json = new JSONObject(result);
                        String status = json.getString("status");
                        String message = json.getString("message");

                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Gagal menghubungi server", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}