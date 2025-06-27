package com.example.uts_a22202303001.ui.profile;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.uts_a22202303001.DataPelanggan;
import com.example.uts_a22202303001.MainLogin;
import com.example.uts_a22202303001.ProfileResponse;
import com.example.uts_a22202303001.R;
import com.example.uts_a22202303001.RegisterAPI;
import com.example.uts_a22202303001.ServerAPI;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private EditText etNama, etAlamat, etKota, etProvinsi, etTelp, etKodePos;
    private Button btnSubmit, btnLogout;
    private ImageView ivProfilePhoto;

    private SharedPreferences sharedPreferences;
    private String email;
    private String currentPhotoPath;
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

        ivProfilePhoto.setOnClickListener(v -> showImagePickerDialog());

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
            new AlertDialog.Builder(requireContext())
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

    private void showImagePickerDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Pilih Gambar");
        builder.setItems(new CharSequence[]{"Galeri", "Kamera"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    openGallery();
                    break;
                case 1:
                    openCamera();
                    break;
            }
        });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
                        requireActivity().getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                ivProfilePhoto.setImageURI(selectedImageUri);
                uploadImage(selectedImageUri);
            } else if (requestCode == CAMERA_REQUEST) {
                File imgFile = new File(currentPhotoPath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    ivProfilePhoto.setImageBitmap(bitmap);
                    uploadImage(Uri.fromFile(imgFile));
                }
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Mengunggah foto...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            // Ambil input stream dari URI
            Log.d("UploadImage", "Uri: " + imageUri.toString());
            InputStream inputStream = requireActivity().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                progressDialog.dismiss();
                Log.e("UploadImage", "Input stream null");
                Toast.makeText(getContext(), "Gagal membaca gambar", Toast.LENGTH_SHORT).show();
                return;
            }

            File imageFile = getFileFromUri(imageUri);

            // Buat RequestBody dan MultipartBody
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part fotoPart = MultipartBody.Part.createFormData("foto", imageFile.getName(), requestFile);
            RequestBody emailPart = RequestBody.create(MediaType.parse("text/plain"), email);

            // Inisialisasi Retrofit
            ServerAPI urlAPI = new ServerAPI();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(urlAPI.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RegisterAPI api = retrofit.create(RegisterAPI.class);
            api.uploadFoto(emailPart, fotoPart).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    progressDialog.dismiss();
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String resString = response.body().string();
                            JSONObject json = new JSONObject(resString);
                            Toast.makeText(getContext(), json.getString("message"), Toast.LENGTH_SHORT).show();

                            if (json.getInt("result") == 1) {
                                // Refresh profil atau aksi lain
                                getProfile(email);
                            }
                        } else {
                            // Coba baca errorBody jika response.body() null
                            String error = response.errorBody() != null ? response.errorBody().string() : "Tidak diketahui";
                            Log.e("UploadFoto", "Error body: " + error);
                            Toast.makeText(getContext(), "Gagal mengunggah: " + error, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Gagal parsing response", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Gagal terhubung: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            progressDialog.dismiss();
            e.printStackTrace();
            Toast.makeText(getContext(), "Terjadi kesalahan saat memproses gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private File getFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("upload", ".jpg", requireActivity().getCacheDir());
        java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile);
        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        inputStream.close();
        return tempFile;
    }

    private void getProfile(String vemail) {
        RegisterAPI api = getRetrofitAPI();
        Call<ProfileResponse> call = api.getProfile(vemail);

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
                        String imageUrl = new ServerAPI().BASE_URL + "image_profile/" + foto;
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
