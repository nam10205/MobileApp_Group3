package com.example.myapplication.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.dal.DBHelper;
import com.example.myapplication.models.Fruit;

import java.util.Calendar;
import java.util.Locale;

// Admin screen to add a new fruit, or update / delete an existing one (when EXTRA_FRUIT_ID is passed).
public class FruitFormActivity extends Activity {
    public static final String EXTRA_FRUIT_ID = "fruitId";

    private DBHelper dbHelper;
    private EditText etName, etCategory, etPrice, etQuantity, etExpiryDate, etDescription;
    private int fruitId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit_form);

        dbHelper = new DBHelper(this);
        etName = findViewById(R.id.etName);
        etCategory = findViewById(R.id.etCategory);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etDescription = findViewById(R.id.etDescription);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);

        fruitId = getIntent().getIntExtra(EXTRA_FRUIT_ID, -1);
        if (fruitId == -1) {
            setTitle("Add fruit");
            btnDelete.setVisibility(View.GONE);
        } else {
            setTitle("Update fruit");
            Fruit fruit = dbHelper.getFruit(fruitId);
            if (fruit == null) {
                Toast.makeText(this, "Fruit not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            etName.setText(fruit.getName());
            etCategory.setText(fruit.getCategory());
            etPrice.setText(String.valueOf(fruit.getPrice()));
            etQuantity.setText(String.valueOf(fruit.getQuantity()));
            etExpiryDate.setText(fruit.getExpiryDate());
            etDescription.setText(fruit.getDescription());
        }

        etExpiryDate.setOnClickListener(v -> pickExpiryDate());
        btnSave.setOnClickListener(v -> save());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void pickExpiryDate() {
        Calendar cal = Calendar.getInstance();
        String current = etExpiryDate.getText().toString();
        if (!current.isEmpty()) {
            String[] parts = current.split("-"); // yyyy-MM-dd
            cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
        }
        new DatePickerDialog(this,
                (view, year, month, day) -> etExpiryDate.setText(
                        String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void save() {
        String name = etName.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String priceText = etPrice.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty() || category.isEmpty() || priceText.isEmpty()
                || quantityText.isEmpty() || expiryDate.isEmpty()) {
            Toast.makeText(this, "Please fill in name, category, price, quantity and expiry date",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        int quantity;
        try {
            price = Double.parseDouble(priceText);
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Price or quantity is not a valid number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (price < 0 || quantity < 0) {
            Toast.makeText(this, "Price and quantity cannot be negative", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean ok = fruitId == -1
                ? dbHelper.insertFruit(name, category, price, quantity, expiryDate, description)
                : dbHelper.updateFruit(fruitId, name, category, price, quantity, expiryDate, description);
        Toast.makeText(this, ok ? "Saved" : "Save failed", Toast.LENGTH_SHORT).show();
        if (ok) {
            finish();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete fruit")
                .setMessage("Are you sure you want to delete this fruit?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean ok = dbHelper.deleteFruit(fruitId);
                    Toast.makeText(this, ok ? "Deleted" : "Delete failed", Toast.LENGTH_SHORT).show();
                    if (ok) {
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
