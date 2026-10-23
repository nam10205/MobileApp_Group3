package com.example.myapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.myapplication.R;
import com.example.myapplication.dal.DBHelper;
import com.example.myapplication.models.Fruit;

import java.util.List;

// Admin home: view and search all fruits, tap one to edit or delete it, add new fruits, manage orders.
public class AdminActivity extends Activity {
    private DBHelper dbHelper;
    private EditText etSearch;
    private ListView lvFruits;
    private List<Fruit> fruits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit_list);

        dbHelper = new DBHelper(this);
        etSearch = findViewById(R.id.etSearch);
        lvFruits = findViewById(R.id.lvFruits);
        lvFruits.setEmptyView(findViewById(R.id.tvEmpty));
        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnOrders = findViewById(R.id.btnOrders);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, FruitFormActivity.class)));
        btnOrders.setOnClickListener(v -> startActivity(new Intent(this, OrderActivity.class)));
        btnLogout.setOnClickListener(v -> LoginActivity.logout(this));

        lvFruits.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, FruitFormActivity.class);
            intent.putExtra(FruitFormActivity.EXTRA_FRUIT_ID, fruits.get(position).getId());
            startActivity(intent);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { loadFruits(); }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFruits(); // refresh after adding, editing or deleting a fruit
    }

    private void loadFruits() {
        fruits = dbHelper.getFruits(etSearch.getText().toString(), false);
        lvFruits.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fruits));
    }
}
