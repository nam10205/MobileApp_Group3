package com.example.myapplication.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.dal.DBHelper;
import com.example.myapplication.models.Customer;
import com.example.myapplication.models.Fruit;

import java.util.List;

// Customer home: view and search available fruits with their expiry dates, place orders, view order history.
public class CustomerActivity extends Activity {
    private DBHelper dbHelper;
    private EditText etSearch;
    private ListView lvFruits;
    private List<Fruit> fruits;
    private Customer customer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit_list);

        dbHelper = new DBHelper(this);
        int accountId = LoginActivity.getPrefs(this).getInt("accountId", -1);
        customer = dbHelper.getCustomerByAccountId(accountId);
        if (customer == null) {
            Toast.makeText(this, "No customer profile for this account", Toast.LENGTH_SHORT).show();
            LoginActivity.logout(this);
            return;
        }
        setTitle("Hello, " + customer.getFullname());

        etSearch = findViewById(R.id.etSearch);
        lvFruits = findViewById(R.id.lvFruits);
        lvFruits.setEmptyView(findViewById(R.id.tvEmpty));
        Button btnOrders = findViewById(R.id.btnOrders);
        Button btnLogout = findViewById(R.id.btnLogout);
        findViewById(R.id.btnAdd).setVisibility(View.GONE); // only admins can add fruits

        btnOrders.setText("My orders");
        btnOrders.setOnClickListener(v -> startActivity(new Intent(this, OrderActivity.class)));
        btnLogout.setOnClickListener(v -> LoginActivity.logout(this));

        lvFruits.setOnItemClickListener((parent, view, position, id) -> showOrderDialog(fruits.get(position)));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { loadFruits(); }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (customer != null) {
            loadFruits();
        }
    }

    // Only fruits that are in stock are shown; expired ones are still listed but marked [EXPIRED].
    private void loadFruits() {
        fruits = dbHelper.getFruits(etSearch.getText().toString(), true);
        lvFruits.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fruits));
    }

    private void showOrderDialog(Fruit fruit) {
        if (fruit.isExpired()) {
            Toast.makeText(this, fruit.getName() + " expired on " + fruit.getExpiryDate()
                    + " and cannot be ordered", Toast.LENGTH_LONG).show();
            return;
        }

        EditText etQuantity = new EditText(this);
        etQuantity.setInputType(InputType.TYPE_CLASS_NUMBER);
        etQuantity.setHint("Quantity (max " + fruit.getQuantity() + ")");

        new AlertDialog.Builder(this)
                .setTitle("Order " + fruit.getName())
                .setMessage(fruit.toString())
                .setView(etQuantity)
                .setPositiveButton("Order", (dialog, which) -> placeOrder(fruit, etQuantity.getText().toString()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void placeOrder(Fruit fruit, String quantityText) {
        int quantity;
        try {
            quantity = Integer.parseInt(quantityText.trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
            return;
        }
        if (quantity <= 0 || quantity > fruit.getQuantity()) {
            Toast.makeText(this, "Quantity must be between 1 and " + fruit.getQuantity(),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.placeOrder(customer.getId(), fruit.getId(), quantity)) {
            Toast.makeText(this, "Order placed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Order failed: fruit is expired or out of stock", Toast.LENGTH_SHORT).show();
        }
        loadFruits();
    }
}
