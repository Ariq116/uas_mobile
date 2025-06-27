package com.example.uts_a22202303001.ui.profile;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.uts_a22202303001.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link KontakKamiFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KontakKamiFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public KontakKamiFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment KontakKamiFragment.
     */
    public static KontakKamiFragment newInstance(String param1, String param2) {
        KontakKamiFragment fragment = new KontakKamiFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_kontak_kami, container, false);

        // ✅ Fungsi tombol "Lihat Lokasi Toko"
        Button btnLokasi = view.findViewById(R.id.btn_lihat_lokasi);
        btnLokasi.setOnClickListener(v -> {
            String url = "https://maps.app.goo.gl/j6U3AvSAaYsP54jD7";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.setPackage("com.google.android.apps.maps"); // prefer Google Maps
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // fallback to browser
                Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(fallbackIntent);
            }
        });

        return view;
    }
}
