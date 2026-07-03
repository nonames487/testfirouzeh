package ai.arena.hisabdar.modern1405.db;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity representing Products/Goods (products) in Firoozeh AI Database.
 * Indexed for lightning-fast search queries using database-level optimization.
 */
@Entity(tableName = "products", indices = {@Index(value = {"name"}), @Index(value = {"barcode"})})
public class Product {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String name;
    private String barcode;
    private double stock;
    private long cost;
    private long salePrice;
    private long lastPurchasePrice; // Used for replacement cost (Real Profit Analysis)
    private String category;

    public Product(String name, String barcode, double stock, long cost, long salePrice, long lastPurchasePrice, String category) {
        this.name = name;
        this.barcode = barcode;
        this.stock = stock;
        this.cost = cost;
        this.salePrice = salePrice;
        this.lastPurchasePrice = lastPurchasePrice;
        this.category = category;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public double getStock() { return stock; }
    public void setStock(double stock) { this.stock = stock; }

    public long getCost() { return cost; }
    public void setCost(long cost) { this.cost = cost; }

    public long getSalePrice() { return salePrice; }
    public void setSalePrice(long salePrice) { this.salePrice = salePrice; }

    public long getLastPurchasePrice() { return lastPurchasePrice; }
    public void setLastPurchasePrice(long lastPurchasePrice) { this.lastPurchasePrice = lastPurchasePrice; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
