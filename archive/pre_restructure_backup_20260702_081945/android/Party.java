package ai.arena.hisabdar.modern1405.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing Business Counterparties (parties) in Firoozeh AI Database.
 */
@Entity(tableName = "parties")
public class Party {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String name;
    private String phone;
    private String nationalId;
    private long balance; // positive means debtor (طلبکار), negative means creditor (بدهکار)

    public Party(String name, String phone, String nationalId, long balance) {
        this.name = name;
        this.phone = phone;
        this.nationalId = nationalId;
        this.balance = balance;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }

    public long getBalance() { return balance; }
    public void setBalance(long balance) { this.balance = balance; }
}
