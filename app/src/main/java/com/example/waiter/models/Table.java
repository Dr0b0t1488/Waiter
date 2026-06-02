package com.example.waiter.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tables")
public class Table {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private int seats;
    private boolean occupied;
    private String establishmentCode;

    public Table() {
        // Required for Firebase
    }

    public Table(int id, String name, int seats, boolean occupied, String establishmentCode) {
        this.id = id;
        this.name = name;
        this.seats = seats;
        this.occupied = occupied;
        this.establishmentCode = establishmentCode;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }
    public String getEstablishmentCode() { return establishmentCode; }
    public void setEstablishmentCode(String establishmentCode) { this.establishmentCode = establishmentCode; }
}
