package com.example.uts_a22202303001;

import static com.example.uts_a22202303001.ServerAPI.BASE_URL_Image;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private ImageView imgProduk;
    private TextView tvNama, tvHarga, tvStok, tvKategori, tvDeskripsi, tvViewCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Inisialisasi komponen
        imgProduk = findViewById(R.id.imgDetail);
        tvNama = findViewById(R.id.tvNamaDetail);
        tvHarga = findViewById(R.id.tvHargaDetail);
        tvStok = findViewById(R.id.tvStokDetail);
        tvKategori = findViewById(R.id.tvKategoriDetail);
        tvDeskripsi = findViewById(R.id.tvDeskripsiDetail);
        tvViewCount = findViewById(R.id.tvViewCountDetail); // ← tambahan view count

        // Ambil produk dari intent
        Product product = (Product) getIntent().getSerializableExtra("produk");

        if (product != null) {
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

            tvNama.setText(product.getNama());
            tvHarga.setText(formatRupiah.format(product.getHargajual()));
            tvStok.setText("Stok: " + product.getStok());
            tvKategori.setText("Kategori: " + product.getKategori());
            tvDeskripsi.setText("Deskripsi: " + product.getDeskripsi());
            tvViewCount.setText("Dilihat: " + product.getJumlah_pengunjung() + " kali"); // ← tampilkan jumlah view

            Glide.with(this)
                    .load(BASE_URL_Image + product.getFoto())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(imgProduk);
        } else {
            Toast.makeText(this, "Data produk tidak ditemukan", Toast.LENGTH_SHORT).show();
        }
    }
}
