package com.example.waiter.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "orders")
public class Order {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int tableId;
    private String status; // e.g., "OPEN", "PAID", "CANCELLED"
    private long timestamp;
    private String establishmentCode;

    public Order() {}

    public Order(int id, int tableId, String status, long timestamp, String establishmentCode) {
        this.id = id;
        this.tableId = tableId;
        this.status = status;
        this.timestamp = timestamp;
        this.establishmentCode = establishmentCode;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getEstablishmentCode() { return establishmentCode; }
    public void setEstablishmentCode(String establishmentCode) { this.establishmentCode = establishmentCode; }
}
