package com.example.waiter.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String imageUrl;
    private String establishmentCode;

    public Category() {
        // Required for Firebase
    }

    public Category(int id, String name, String imageUrl, String establishmentCode) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.establishmentCode = establishmentCode;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getEstablishmentCode() { return establishmentCode; }
    public void setEstablishmentCode(String establishmentCode) { this.establishmentCode = establishmentCode; }
}
