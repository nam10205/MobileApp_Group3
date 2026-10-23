package com.example.myapplication.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.dal.DBHelper;
import com.example.myapplication.models.Account;
import com.example.myapplication.models.Customer;
import com.example.myapplication.models.Order;

import java.util.List;

// Admin: sees all orders and taps one to change its status.
// Customer: sees their own order history.
public class OrderActivity extends Activity {
    private DBHelper dbHelper;
    private ListView lvOrders;
    private List<Order> orders;
    private Integer customerId; // null for admin = all orders

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        dbHelper = new DBHelper(this);
        lvOrders = findViewById(R.id.lvOrders);
        lvOrders.setEmptyView(findViewById(R.id.tvEmpty));

        boolean isAdmin = Account.ROLE_ADMIN.equals(
                LoginActivity.getPrefs(this).getString("role", null));
        if (isAdmin) {
            setTitle("All orders");
            lvOrders.setOnItemClickListener((parent, view, position, id) ->
                    showStatusDialog(orders.get(position)));
        } else {
            setTitle("My orders");
            int accountId = LoginActivity.getPrefs(this).getInt("accountId", -1);
            Customer customer = dbHelper.getCustomerByAccountId(accountId);
            if (customer == null) {
                finish();
                return;
            }
            customerId = customer.getId();
        }

        loadOrders();
    }

    private void loadOrders() {
        orders = dbHelper.getOrders(customerId);
        lvOrders.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, orders));
    }

    private void showStatusDialog(Order order) {
        new AlertDialog.Builder(this)
                .setTitle("Order #" + order.getId() + " status")
                .setItems(Order.STATUSES, (dialog, which) -> {
                    boolean ok = dbHelper.updateOrderStatus(order.getId(), Order.STATUSES[which]);
                    Toast.makeText(this, ok ? "Status updated" : "Update failed", Toast.LENGTH_SHORT).show();
                    loadOrders();
                })
                .show();
    }
}
