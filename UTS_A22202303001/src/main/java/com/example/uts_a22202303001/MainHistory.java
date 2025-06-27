package com.example.uts_a22202303001;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uts_a22202303001.model.OrderHistory;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainHistory extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_history); // Pastikan kamu membuat layout ini

        recyclerView = findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadOrderHistory();
    }

    private void loadOrderHistory() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");

        RegisterAPI api = ApiClient.getClient(ServerAPI.BASE_URL).create(RegisterAPI.class);
        Call<List<OrderHistory>> call = api.getOrderHistory(email);

        call.enqueue(new Callback<List<OrderHistory>>() {
            @Override
            public void onResponse(Call<List<OrderHistory>> call, Response<List<OrderHistory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new OrderHistoryAdapter(MainHistory.this, response.body(), order -> {
                        // Aksi saat tombol "Upload Bukti" ditekan
                        Toast.makeText(MainHistory.this, "Upload bukti untuk Order -" + order.getOrderId(), Toast.LENGTH_SHORT).show();
                    });
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(MainHistory.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderHistory>> call, Throwable t) {
                Toast.makeText(MainHistory.this, "Gagal koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
   });
}
}