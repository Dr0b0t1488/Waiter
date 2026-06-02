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

    public Table(int id, String name, int seats, boolean occupied) {
        this.id = id;
        this.name = name;
        this.seats = seats;
        this.occupied = occupied;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }
}
