package com.example.myapplication.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myapplication.models.Account;
import com.example.myapplication.models.Customer;
import com.example.myapplication.models.Fruit;
import com.example.myapplication.models.Order;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "FruitShopDB";
    public static final int DB_VERSION = 1;

    private static final String ORDER_TABLE = "\"Order\"";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createAccount =
                "CREATE TABLE Account(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT UNIQUE NOT NULL," +
                        "password TEXT NOT NULL," +
                        "role TEXT NOT NULL)";

        String createFruit =
                "CREATE TABLE Fruit(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "category TEXT," +
                        "price REAL NOT NULL," +
                        "quantity INTEGER NOT NULL," +
                        "expiryDate TEXT NOT NULL," +
                        "description TEXT)";

        String createCustomer =
                "CREATE TABLE Customer(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "fullname TEXT NOT NULL," +
                        "phone TEXT," +
                        "email TEXT," +
                        "accountId INTEGER NOT NULL," +
                        "FOREIGN KEY (accountId) REFERENCES Account(id))";

        String createOrder =
                "CREATE TABLE " + ORDER_TABLE + "(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "customerId INTEGER NOT NULL," +
                        "orderDate TEXT NOT NULL," +
                        "totalAmount REAL NOT NULL," +
                        "status TEXT NOT NULL," +
                        "FOREIGN KEY (customerId) REFERENCES Customer(id))";

        db.execSQL(createAccount);
        db.execSQL(createFruit);
        db.execSQL(createCustomer);
        db.execSQL(createOrder);

        db.execSQL("INSERT INTO Account(username,password,role) VALUES('admin','" + hashPassword("admin123") + "','Admin')");
        db.execSQL("INSERT INTO Account(username,password,role) VALUES('customer','" + hashPassword("123456") + "','Customer')");

        db.execSQL("INSERT INTO Customer(fullname,phone,email,accountId) " +
                "VALUES('Nguyen Van A','0901234567','customer@fruitshop.com',2)");

        db.execSQL("INSERT INTO Fruit(name,category,price,quantity,expiryDate,description) " +
                "VALUES('Apple','Imported',2.5,50,'2027-12-31','Red apple')");
        db.execSQL("INSERT INTO Fruit(name,category,price,quantity,expiryDate,description) " +
                "VALUES('Banana','Local',1.0,100,'2027-12-31','Yellow banana')");
        db.execSQL("INSERT INTO Fruit(name,category,price,quantity,expiryDate,description) " +
                "VALUES('Mango','Local',3.0,20,'2025-01-01','Expired sample')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ORDER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS Customer");
        db.execSQL("DROP TABLE IF EXISTS Fruit");
        db.execSQL("DROP TABLE IF EXISTS Account");
        onCreate(db);
    }

    // ---------- Account / Customer ----------

    public boolean insertAccount(String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", hashPassword(password));
        values.put("role", role);
        long result = db.insert("Account", null, values);
        return result != -1;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public boolean insertCustomer(String fullname, String phone, String email, int accountId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fullname", fullname);
        values.put("phone", phone);
        values.put("email", email);
        values.put("accountId", accountId);
        long result = db.insert("Customer", null, values);
        return result != -1;
    }

    public boolean checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM Account WHERE username=? AND password=?",
                new String[]{username, password}
        );
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public Account getAccount(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, username, password, role FROM Account WHERE username=?",
                new String[]{username}
        );
        Account account = null;
        if (cursor.moveToFirst()) {
            account = new Account(cursor.getInt(0), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3));
        }
        cursor.close();
        return account;
    }

    public Customer getCustomerByAccountId(int accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, fullname, phone, email, accountId FROM Customer WHERE accountId=?",
                new String[]{String.valueOf(accountId)}
        );
        Customer customer = null;
        if (cursor.moveToFirst()) {
            customer = new Customer(cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getInt(4));
        }
        cursor.close();
        return customer;
    }

    public boolean insertFruit(String name, String category, double price, int quantity,
                               String expiryDate, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("category", category);
        values.put("price", price);
        values.put("quantity", quantity);
        values.put("expiryDate", expiryDate);
        values.put("description", description);
        long result = db.insert("Fruit", null, values);
        return result != -1;
    }

    public boolean updateFruit(int id, String name, String category, double price, int quantity,
                               String expiryDate, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("category", category);
        values.put("price", price);
        values.put("quantity", quantity);
        values.put("expiryDate", expiryDate);
        values.put("description", description);
        int result = db.update("Fruit", values, "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean deleteFruit(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("Fruit", "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    // Searches by name or category. onlyInStock limits results to fruits with quantity > 0.
    public List<Fruit> getFruits(String keyword, boolean onlyInStock) {
        SQLiteDatabase db = this.getReadableDatabase();
        String like = "%" + keyword.trim() + "%";
        Cursor cursor = db.rawQuery(
                "SELECT id, name, category, price, quantity, expiryDate, description FROM Fruit " +
                        "WHERE (name LIKE ? OR category LIKE ?)" +
                        (onlyInStock ? " AND quantity > 0" : "") +
                        " ORDER BY name",
                new String[]{like, like}
        );
        List<Fruit> fruits = new ArrayList<>();
        while (cursor.moveToNext()) {
            fruits.add(readFruit(cursor));
        }
        cursor.close();
        return fruits;
    }

    public Fruit getFruit(int id) {
        return getFruit(this.getReadableDatabase(), id);
    }

    private Fruit getFruit(SQLiteDatabase db, int id) {
        Cursor cursor = db.rawQuery(
                "SELECT id, name, category, price, quantity, expiryDate, description FROM Fruit WHERE id=?",
                new String[]{String.valueOf(id)}
        );
        Fruit fruit = null;
        if (cursor.moveToFirst()) {
            fruit = readFruit(cursor);
        }
        cursor.close();
        return fruit;
    }

    private Fruit readFruit(Cursor cursor) {
        return new Fruit(cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                cursor.getDouble(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6));
    }

    public boolean placeOrder(int customerId, int fruitId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            Fruit fruit = getFruit(db, fruitId);
            if (fruit == null || fruit.isExpired() || quantity <= 0 || quantity > fruit.getQuantity()) {
                return false;
            }

            ContentValues stock = new ContentValues();
            stock.put("quantity", fruit.getQuantity() - quantity);
            db.update("Fruit", stock, "id=?", new String[]{String.valueOf(fruitId)});

            ContentValues values = new ContentValues();
            values.put("customerId", customerId);
            values.put("orderDate",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date()));
            values.put("totalAmount", fruit.getPrice() * quantity);
            values.put("status", Order.STATUS_PENDING);
            long result = db.insert(ORDER_TABLE, null, values);
            if (result == -1) {
                return false;
            }

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public List<Order> getOrders(Integer customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT id, customerId, orderDate, totalAmount, status FROM " + ORDER_TABLE;
        String[] args = null;
        if (customerId != null) {
            sql += " WHERE customerId=?";
            args = new String[]{String.valueOf(customerId)};
        }
        Cursor cursor = db.rawQuery(sql + " ORDER BY id DESC", args);
        List<Order> orders = new ArrayList<>();
        while (cursor.moveToNext()) {
            orders.add(new Order(cursor.getInt(0), cursor.getInt(1), cursor.getString(2),
                    cursor.getDouble(3), cursor.getString(4)));
        }
        cursor.close();
        return orders;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        int result = db.update(ORDER_TABLE, values, "id=?", new String[]{String.valueOf(orderId)});
        return result > 0;
    }
}
