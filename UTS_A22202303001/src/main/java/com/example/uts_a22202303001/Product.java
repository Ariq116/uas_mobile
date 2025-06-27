package com.example.uts_a22202303001;

import java.io.Serializable;

public class Product implements Serializable {
    private String foto;
    private String nama;
    private double hargajual;
    private int stok;
    private int quantity;
    private String kode;
    private String kategori;
    private String deskripsi;
    private String id_produk;

    private int jumlah_pengunjung;

    public Product(String id_produk, String foto, String nama, double hargajual, int stok, String kategori, String deskripsi, int jumlah_pengunjung) {
        this.id_produk = id_produk;
        this.foto = foto;
        this.nama = nama;
        this.hargajual = hargajual;
        this.stok = stok;
        this.kategori = kategori;
        this.deskripsi = deskripsi;
        this.jumlah_pengunjung = jumlah_pengunjung;
}

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public double getHargajual() {
        return hargajual;
    }

    public void setHargajual(double hargajual) {
        this.hargajual = hargajual;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return hargajual * quantity;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    // Getter dan setter untuk jumlah dilihat

    public String getId_produk() { return id_produk;
    }

    public String getKode() { return kode;
    }

    public int getJumlah_pengunjung() { return jumlah_pengunjung; }
    public void setJumlah_pengunjung(int jumlah_pengunjung) { this.jumlah_pengunjung = jumlah_pengunjung;}
}
