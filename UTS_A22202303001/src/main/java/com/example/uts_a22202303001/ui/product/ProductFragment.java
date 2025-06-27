package com.example.uts_a22202303001.ui.product;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uts_a22202303001.DetailActivity;
import com.example.uts_a22202303001.Product;
import com.example.uts_a22202303001.R;
import com.example.uts_a22202303001.RegisterAPI;
import com.example.uts_a22202303001.ServerAPI;
import com.example.uts_a22202303001.databinding.FragmentProductBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductFragment extends Fragment {
    private FragmentProductBinding binding;
    private ProductAdapter adapter;
    private ArrayList<Product> productList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.recyclerProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new ProductAdapter(productList, requireContext());
        binding.recyclerProduct.setAdapter(adapter);

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        loadProducts();
        return view;
    }

    private void loadProducts() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        Call<List<Product>> call = api.getProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    adapter.setOriginalList(new ArrayList<>(productList));
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat produk", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
        private ArrayList<Product> list;
        private ArrayList<Product> originalList;
        private Context context;

        public ProductAdapter(ArrayList<Product> list, Context context) {
            this.list = new ArrayList<>(list);
            this.originalList = new ArrayList<>(list);
            this.context = context;
        }

        public void setOriginalList(ArrayList<Product> originalList) {
            this.originalList = new ArrayList<>(originalList);
            this.list = new ArrayList<>(originalList);
        }

        public void filter(String query) {
            list.clear();
            if (query == null || query.trim().isEmpty()) {
                list.addAll(originalList);
            } else {
                String lowerQuery = query.toLowerCase();
                for (Product p : originalList) {
                    if (p.getNama().toLowerCase().contains(lowerQuery)) {
                        list.add(p);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_produk, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Product product = list.get(position);

            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            holder.tvNama.setText(product.getNama());
            holder.tvHarga.setText(formatRupiah.format(product.getHargajual()));
            holder.tvStok.setText("Stok: " + product.getStok());
            holder.tvView.setText("Dilihat: " + product.getJumlah_pengunjung());

            Glide.with(context)
                    .load(ServerAPI.BASE_URL_Image + product.getFoto())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imgProduk);

            holder.btnOrder.setOnClickListener(v -> {
                SharedPreferences pref = context.getSharedPreferences("pref_product", Context.MODE_PRIVATE);
                Gson gson = new Gson();
                String json = pref.getString("listorder", null);
                Type type = new TypeToken<ArrayList<Product>>() {}.getType();
                ArrayList<Product> orderList = gson.fromJson(json, type);
                if (orderList == null) orderList = new ArrayList<>();

                int currentQtyInOrder = getQuantityFromOrderList(orderList, product.getNama());
                int stokAwal = product.getStok() + currentQtyInOrder;

                if (currentQtyInOrder >= stokAwal) {
                    Toast.makeText(context, "Stok tidak mencukupi", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean found = false;
                for (Product p : orderList) {
                    if (p.getNama().equals(product.getNama())) {
                        p.setQuantity(p.getQuantity() + 1);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Product newProduct = new Product(
                            product.getId_produk(),
                            product.getFoto(),
                            product.getNama(),
                            product.getHargajual(),
                            product.getStok(),
                            product.getKategori(),
                            product.getDeskripsi(),
                            product.getJumlah_pengunjung()
                    );
                    newProduct.setQuantity(1);
                    orderList.add(newProduct);
                }

                product.setStok(product.getStok() - 1);
                holder.tvStok.setText("Stok: " + product.getStok());

                pref.edit().putString("listorder", gson.toJson(orderList)).apply();

                Toast.makeText(context, "Ditambahkan ke checkout", Toast.LENGTH_SHORT).show();
            });

            holder.btnDetail.setOnClickListener(v -> {
                // Kirim ke DetailActivity
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("produk", product);
                context.startActivity(intent);

                // Panggil API untuk update jumlah pengunjung
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(ServerAPI.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                RegisterAPI api = retrofit.create(RegisterAPI.class);
                Call<ResponseBody> call = api.updateViewer(product.getId_produk());

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            int updatedViewer = product.getJumlah_pengunjung() + 1;
                            product.setJumlah_pengunjung(updatedViewer);
                            holder.tvView.setText("Dilihat: " + updatedViewer);
                            notifyItemChanged(holder.getAdapterPosition());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(context, "Gagal update jumlah pengunjung", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        private int getQuantityFromOrderList(ArrayList<Product> orderList, String namaProduk) {
            for (Product p : orderList) {
                if (p.getNama().equals(namaProduk)) {
                    return p.getQuantity();
                }
            }
            return 0;
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNama, tvHarga, tvStok, tvView;
            ImageView imgProduk, btnOrder, btnDetail;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNama = itemView.findViewById(R.id.tvNama);
                tvHarga = itemView.findViewById(R.id.tvHarga);
                tvStok = itemView.findViewById(R.id.tvStok);
                tvView = itemView.findViewById(R.id.tvJumlahPengunjung); // jumlah pengunjung
                imgProduk = itemView.findViewById(R.id.imgProduk);
                btnOrder = itemView.findViewById(R.id.btnOrder);
                btnDetail = itemView.findViewById(R.id.btnDetail);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
