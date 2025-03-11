package lk.jiat.fitmart;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lk.jiat.fitmart.model.GridSpacingItemDecoration;
import lk.jiat.fitmart.model.Product;
import lk.jiat.fitmart.model.SQLiteHelper;
import lk.jiat.fitmart.model.SuggesterProductsAdapter;

public class AdvancedSearchActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_advanced_search);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.button));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupSpinners();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SuggesterProductsAdapter adapter = new SuggesterProductsAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> performAdvancedSearch());
    }

    private void setupSpinners() {
        SQLiteHelper sqLiteHelper = new SQLiteHelper(AdvancedSearchActivity.this, "fitmart.db", null, 1);
        SQLiteDatabase sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        List<String> categoryList = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM `category`", null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String category = cursor.getString(1);
                categoryList.add(category);
            }
            cursor.close();
        } else {
            Log.e("FitMartLog", "No categories found or cursor is null");
        }
        categoryList.add(0, "Category");

        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        List<String> sortByOptions = Arrays.asList("Relevance", "Price: Low to High", "Price: High to Low");
        ArrayAdapter<String> sortByAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortByOptions);
        sortByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sortBySpinner = findViewById(R.id.sortBySpinner);
        sortBySpinner.setAdapter(sortByAdapter);
    }

    private void performAdvancedSearch() {
        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        EditText minPrice = findViewById(R.id.minPrice);
        EditText maxPrice = findViewById(R.id.maxPrice);
        Spinner sortBySpinner = findViewById(R.id.sortBySpinner);
        CheckBox inStockCheckbox = findViewById(R.id.inStockCheckbox);

        String category = categorySpinner.getSelectedItem().toString();
        double minPriceValue = 0;
        double maxPriceValue = Double.MAX_VALUE;

        if (!minPrice.getText().toString().isEmpty()) {
            try {
                minPriceValue = Double.parseDouble(minPrice.getText().toString());
            } catch (NumberFormatException e) {
                Log.e("AdvancedSearch", "Invalid min price input");
                Toast.makeText(this, "Invalid min price input", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!maxPrice.getText().toString().isEmpty()) {
            try {
                maxPriceValue = Double.parseDouble(maxPrice.getText().toString());
            } catch (NumberFormatException e) {
                Log.e("AdvancedSearch", "Invalid max price input");
                Toast.makeText(this, "Invalid max price input", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (minPriceValue > maxPriceValue) {
            Log.e("AdvancedSearch", "Min price cannot be greater than max price");
            Toast.makeText(this, "Min price cannot be greater than max price", Toast.LENGTH_SHORT).show();
            return;
        }

        String sortBy = sortBySpinner.getSelectedItem().toString();
        boolean inStockOnly = inStockCheckbox.isChecked();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("product");

        if (!category.equals("Category")) {
            query = query.whereEqualTo("category", category);
        }

        double finalMaxPriceValue = maxPriceValue;
        double finalMinPriceValue = minPriceValue;
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Product> filteredProducts = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String name = document.getString("title");
                String priceStr = document.getString("price");
                String qtyStr = document.getString("qty");
                String imageUrl = document.getString("imageUrl");

                if (name == null || priceStr == null || qtyStr == null || imageUrl == null) {
                    continue;
                }

                double price = 0;
                int qty = 0;
                try {
                    price = Double.parseDouble(priceStr.replaceAll("[^\\d.]", ""));
                    qty = Integer.parseInt(qtyStr.replaceAll("[^\\d]", ""));
                } catch (NumberFormatException e) {
                    Log.e("AdvancedSearch", "Error parsing price or qty: " + priceStr + ", " + qtyStr);
                    continue;
                }

                // Apply filters
                if (price >= finalMinPriceValue && price <= finalMaxPriceValue) {
                    if (!inStockOnly || qty > 0) {
                        Product product = new Product(name, priceStr, imageUrl);
                        filteredProducts.add(product);
                    }
                }
            }

            switch (sortBy) {
                case "Price: Low to High":
                    filteredProducts.sort((p1, p2) -> {
                        double price1 = Double.parseDouble(p1.getPrice().replaceAll("[^\\d.]", ""));
                        double price2 = Double.parseDouble(p2.getPrice().replaceAll("[^\\d.]", ""));
                        return Double.compare(price1, price2);
                    });
                    break;
                case "Price: High to Low":
                    filteredProducts.sort((p1, p2) -> {
                        double price1 = Double.parseDouble(p1.getPrice().replaceAll("[^\\d.]", ""));
                        double price2 = Double.parseDouble(p2.getPrice().replaceAll("[^\\d.]", ""));
                        return Double.compare(price2, price1);
                    });
                    break;
                default:
                    filteredProducts.sort((p1, p2) -> p1.getTitle().compareToIgnoreCase(p2.getTitle()));
                    break;
            }

            updateProductRecyclerView(filteredProducts);
        }).addOnFailureListener(e -> {
            Log.e("AdvancedSearch", "Firestore query failed", e);
            runOnUiThread(() -> {
                Toast.makeText(AdvancedSearchActivity.this, "Failed to fetch products", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateProductRecyclerView(List<Product> productList) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        int spanCount = 2;
        int spacing = 16;
        boolean includeEdge = true;
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));


        SuggesterProductsAdapter adapter = (SuggesterProductsAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.updateData(productList);
        } else {
            Log.e("AdvancedSearch", "RecyclerView adapter is null");
        }
    }
}