package lk.jiat.fitmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lk.jiat.fitmart.model.Cart;
import lk.jiat.fitmart.model.InvoiceAdapter;

public class InvoiceActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private InvoiceAdapter invoiceAdapter;
    private List<Cart> purchasedItems;
    private String qty;
    private double subtotal = 0;
    private double shipping = 0;
    private double total = 0;
    private String Usermobile;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.button));

        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        Usermobile = sharedPreferences.getString("mobile", "0714833745");

        recyclerView = findViewById(R.id.invoiceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("PURCHASED_ITEMS")) {
            purchasedItems = (List<Cart>) intent.getSerializableExtra("PURCHASED_ITEMS");
        } else {
            purchasedItems = new ArrayList<>();
            Log.e("PayHere_DebugInvoice", "No purchased items found in intent");
        }

        Log.d("PayHere_DebugInvoice", "Purchased Items: " + purchasedItems);
        calculatePrices();

        // Set up adapter
        invoiceAdapter = new InvoiceAdapter(this, purchasedItems);
        recyclerView.setAdapter(invoiceAdapter);

        Log.d("PayHere_DebugInvoice", "Adapter Item Count: " + invoiceAdapter.getItemCount());
        updatePriceDisplays();

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Process invoice creation, cart deletion, and product updates
        processInvoiceAndUpdates(intent.getStringExtra("ORDER_ID"), intent.getStringExtra("CUSTOMER_NAME"), Usermobile);
    }

    private void calculatePrices() {
        for (Cart item : purchasedItems) {
            try {
                qty = String.valueOf(item.getQuantity());
                String priceStr = item.getPrice().replaceAll("[^\\d.]", "");
                double price = Double.parseDouble(priceStr);
                subtotal += (price * item.getQuantity());

                // Calculate shipping for each item
                String shippingStr = item.getDeliveryfee().replaceAll("[^\\d.]", "");
                double itemShipping = Double.parseDouble(shippingStr);
                shipping += itemShipping;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        total = subtotal + shipping;
    }

    private void updatePriceDisplays() {
        TextView subtotalText = findViewById(R.id.subtotalAmount);
        TextView shippingText = findViewById(R.id.shippingAmount);
        TextView totalText = findViewById(R.id.totalAmount);

        subtotalText.setText(String.format("Subtotal: Rs. %.2f", subtotal));
        shippingText.setText(String.format("Shipping: Rs. %.2f", shipping));
        totalText.setText(String.format("Total: Rs. %.2f", total));
    }

    private void processInvoiceAndUpdates(String orderId, String customerName, String usermobile) {
        Map<String, Object> invoiceData = new HashMap<>();
        invoiceData.put("orderId", orderId);
        invoiceData.put("customerName", customerName);
        invoiceData.put("customerMobile", usermobile);
        invoiceData.put("totalAmount", total);
        invoiceData.put("purchaseDate", com.google.firebase.firestore.FieldValue.serverTimestamp());

        List<Map<String, Object>> invoiceItems = new ArrayList<>();
        for (Cart item : purchasedItems) {
            Map<String, Object> invoiceItem = new HashMap<>();
            invoiceItem.put("productName", item.getTitle());
            invoiceItem.put("quantity", item.getQuantity());
            invoiceItem.put("price", item.getPrice());
            invoiceItem.put("status", "Pending");
            invoiceItems.add(invoiceItem);
        }
        invoiceData.put("items", invoiceItems);

        firestore.collection("invoices").document(orderId).set(invoiceData)
                .addOnSuccessListener(aVoid -> Log.d("InvoiceActivity", "Invoice created successfully"))
                .addOnFailureListener(e -> Log.e("InvoiceActivity", "Error creating invoice", e));


        for (Cart item : purchasedItems) {
            firestore.collection("cart")
                    .whereEqualTo("title", item.getTitle()) // Use title as the unique identifier
                    .whereEqualTo("mobile", Usermobile)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> Log.d("InvoiceActivity", "Cart item deleted: " + document.getId()))
                                    .addOnFailureListener(e -> Log.e("InvoiceActivity", "Error deleting cart item", e));
                        }
                    })
                    .addOnFailureListener(e -> Log.e("InvoiceActivity", "Error fetching cart items", e));
        }

        for (Cart item : purchasedItems) {
            firestore.collection("product")
                    .whereEqualTo("title", item.getTitle()) // Use title as the unique identifier
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            String currentStockStr = documentSnapshot.getString("qty");
                            if (currentStockStr != null) {
                                int currentStock = Integer.parseInt(currentStockStr);
                                int newStock = currentStock - item.getQuantity();
                                if (newStock >= 0) {
                                    documentSnapshot.getReference().update("qty", String.valueOf(newStock)) // Update stock as a string
                                            .addOnSuccessListener(aVoid -> Log.d("InvoiceActivity", "Product stock updated: " + item.getTitle()))
                                            .addOnFailureListener(e -> Log.e("InvoiceActivity", "Error updating product stock", e));
                                } else {
                                    Log.e("InvoiceActivity", "Insufficient stock for product: " + item.getTitle());
                                }
                            } else {
                                Log.e("InvoiceActivity", "Stock field missing for product: " + item.getTitle());
                            }
                        } else {
                            Log.e("InvoiceActivity", "Product not found: " + item.getTitle());
                        }
                    })
                    .addOnFailureListener(e -> Log.e("InvoiceActivity", "Error fetching product details", e));
        }
    }
}