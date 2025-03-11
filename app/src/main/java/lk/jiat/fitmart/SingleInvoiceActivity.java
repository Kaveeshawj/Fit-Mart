package lk.jiat.fitmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.jiat.fitmart.model.InvoiceAdapter;

public class SingleInvoiceActivity extends AppCompatActivity {
    private Integer qty;
    private String price;
    private double subtotal = 0;
    private double shipping = 0;
    private double total = 0;
    private String Usermobile;
    private FirebaseFirestore firestore;
    private String title;
    private RecyclerView recyclerView;
    private List<Map<String, Object>> purchasedItems = new ArrayList<Map<String, Object>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_single_invoice);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.button));

        firestore = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        Usermobile = sharedPreferences.getString("mobile", "0714833745");

        recyclerView = findViewById(R.id.invoiceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        TextView subtotalText = findViewById(R.id.subtotalAmount);
        TextView shippingText = findViewById(R.id.shippingAmount);
        TextView totalText = findViewById(R.id.totalAmount);

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("title") || !intent.hasExtra("qty") || !intent.hasExtra("price")) {
            Log.e("InvoiceActivity", "Invalid intent or missing extras");
            finish();
            return;
        }

        title = intent.getStringExtra("title");
        qty = intent.getIntExtra("qty", 1);
        price = intent.getStringExtra("price");

        try {
            double intentPrice = Double.parseDouble(price);
            subtotal = intentPrice * qty;
            shipping = 100;
            total = subtotal + shipping;
        } catch (NumberFormatException e) {
            Log.e("InvoiceActivity", "Invalid price or quantity", e);
            finish();
            return;
        }

        // Update price displays
        subtotalText.setText(String.format("Subtotal: Rs. %.2f", subtotal));
        shippingText.setText(String.format("Shipping: Rs. %.2f", shipping));
        totalText.setText(String.format("Total: Rs. %.2f", total));

        // Add the product to the purchasedItems list
        Map<String, Object> product = new HashMap<>();
        product.put("title", title);
        product.put("price", price);
        product.put("qty", qty);
        purchasedItems.add(product);

        // Set up the RecyclerView adapter
        InvoiceAdapter invoiceAdapter = new InvoiceAdapter(this, purchasedItems);
        recyclerView.setAdapter(invoiceAdapter);

        // Process invoice and updates
        processInvoiceAndUpdates(
                intent.getStringExtra("ORDER_ID"),
                intent.getStringExtra("CUSTOMER_NAME"),
                title,
                Usermobile
        );
    }
    private void processInvoiceAndUpdates(String orderId, String customerName, String title, String usermobile) {
        Map<String, Object> invoiceData = new HashMap<>();
        invoiceData.put("orderId", orderId);
        invoiceData.put("customerName", customerName);
        invoiceData.put("customerMobile", usermobile);
        invoiceData.put("totalAmount", total);
        invoiceData.put("purchaseDate", com.google.firebase.firestore.FieldValue.serverTimestamp());

        Map<String, Object> invoiceItem = new HashMap<>();
        invoiceItem.put("productName", title);
        invoiceItem.put("quantity", qty);
        invoiceItem.put("price", price);
        invoiceItem.put("status", "Pending");

        List<Map<String, Object>> invoiceItems = new ArrayList<>();
        invoiceItems.add(invoiceItem);
        invoiceData.put("items", invoiceItems);

        firestore.collection("invoices").document(orderId).set(invoiceData)
                .addOnSuccessListener(aVoid -> Log.d("InvoiceActivity", "Invoice created successfully"))
                .addOnFailureListener(e -> Log.e("InvoiceActivity", "Error creating invoice", e));

        firestore.collection("product")
                .whereEqualTo("title", title)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String currentStockStr = documentSnapshot.getString("qty");
                        if (currentStockStr != null) {
                            int currentStock = Integer.parseInt(currentStockStr);
                            //int purchasedQty = Integer.parseInt(qty);
                            int newStock = currentStock - qty;
                            if (newStock >= 0) {
                                documentSnapshot.getReference().update("qty", String.valueOf(newStock))
                                        .addOnSuccessListener(aVoid -> Log.d("InvoiceActivity", "Product stock updated: " + title))
                                        .addOnFailureListener(e -> Log.e("InvoiceActivity", "Error updating product stock", e));
                            } else {
                                Log.e("InvoiceActivity", "Insufficient stock for product: " + title);
                            }
                        } else {
                            Log.e("InvoiceActivity", "Stock field missing for product: " + title);
                        }
                    } else {
                        Log.e("InvoiceActivity", "Product not found: " + title);
                    }
                })
                .addOnFailureListener(e -> Log.e("InvoiceActivity", "Error fetching product details", e));
    }
}