package com.example.uts_a22202303001.ui.profile;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.uts_a22202303001.DataPelanggan;
import com.example.uts_a22202303001.MainLogin;
import com.example.uts_a22202303001.ProfileResponse;
import com.example.uts_a22202303001.R;
import com.example.uts_a22202303001.RegisterAPI;
import com.example.uts_a22202303001.ServerAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditProfileFragment extends Fragment {

    private EditText etNama, etAlamat, etKota, etProvinsi, etTelp, etKodePos;
    private Button btnSubmit, btnLogout;
    private ImageView ivProfilePhoto;

    private SharedPreferences sharedPreferences;
    private String email;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivProfilePhoto.setImageURI(selectedImageUri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        sharedPreferences = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");

        etNama = view.findViewById(R.id.etProfileNama);
        etAlamat = view.findViewById(R.id.etProfile_Alamat);
        etKota = view.findViewById(R.id.etProfile_Kota);
        etProvinsi = view.findViewById(R.id.etProfile_Provinsi);
        etTelp = view.findViewById(R.id.etProfile_Telp);
        etKodePos = view.findViewById(R.id.etProfile_Kodepos);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnLogout = view.findViewById(R.id.tvProfile_Back);
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);

        // Load foto dari SharedPreferences
        String fotoSaved = sharedPreferences.getString("foto", "");
        if (!fotoSaved.isEmpty()) {
            String imageUrl = new ServerAPI().BASE_URL + "Image_Profile/" + fotoSaved;
            Glide.with(requireContext())
                    .load(imageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.kean_logo)
                    .error(R.drawable.kean_logo)
                    .into(ivProfilePhoto);
        } else {
            ivProfilePhoto.setImageResource(R.drawable.kean_logo);
        }

        ivProfilePhoto.setOnClickListener(v -> {
            if (hasImagePermission()) {
                openGallery();
            } else {
                requestImagePermission();
            }
        });

        btnSubmit.setOnClickListener(v -> {
            MultipartBody.Part photoPart = null;

            if (selectedImageUri != null) {
                try {
                    File imageFile = createTempFileFromUri(selectedImageUri);
                    RequestBody requestFile = RequestBody.create(imageFile, MediaType.parse("image/*"));
                    photoPart = MultipartBody.Part.createFormData("foto", imageFile.getName(), requestFile);
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Gagal membaca gambar.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            RegisterAPI api = getRetrofitAPI();
            Call<ResponseBody> call = api.updateProfile(
                    toRequestBody(etNama.getText().toString()),
                    toRequestBody(etAlamat.getText().toString()),
                    toRequestBody(etKota.getText().toString()),
                    toRequestBody(etProvinsi.getText().toString()),
                    toRequestBody(etTelp.getText().toString()),
                    toRequestBody(etKodePos.getText().toString()),
                    toRequestBody(email),
                    photoPart
            );

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(getContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                        selectedImageUri = null;
                    } else {
                        Toast.makeText(getContext(), "Gagal memperbarui profil", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getContext(), "Gagal: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Logout")
                    .setMessage("Apakah kamu yakin ingin logout?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        sharedPreferences.edit().clear().apply();
                        Intent intent = new Intent(getActivity(), MainLogin.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        getProfile(email);
        return view;
    }

    private void getProfile(String vemail) {
        RegisterAPI api = getRetrofitAPI();
        Call<ProfileResponse> call = api.getProfile(vemail);  // Ganti ResponseBody jadi ProfileResponse

        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() == 1) {
                    DataPelanggan data = response.body().getData();
                    etNama.setText(data.getNama());
                    etAlamat.setText(data.getAlamat());
                    etKota.setText(data.getKota());
                    etProvinsi.setText(data.getProvinsi());
                    etTelp.setText(data.getTelp());
                    etKodePos.setText(data.getKodepos());

                    String foto = data.getFoto();
                    if (foto != null && !foto.isEmpty()) {
                        String imageUrl = new ServerAPI().BASE_URL + "Image_Profile/" + foto;
                        Glide.with(requireContext())
                                .load(imageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.kean_logo)
                                .error(R.drawable.kean_logo)
                                .into(ivProfilePhoto);

                        sharedPreferences.edit().putString("foto", foto).apply();
                    }
                } else {
                    Toast.makeText(getContext(), "Gagal memuat profil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Koneksi gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean hasImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private File createTempFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
        File tempFile = new File(requireContext().getCacheDir(), fileName);
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while (inputStream != null && (bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    private RegisterAPI getRetrofitAPI() {
        return new Retrofit.Builder()
                .baseUrl(new ServerAPI().BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RegisterAPI.class);
    }

    private RequestBody toRequestBody(String value) {
        return RequestBody.create(value, MediaType.parse("text/plain"));
    }
}
