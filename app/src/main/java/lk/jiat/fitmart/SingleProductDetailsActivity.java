package lk.jiat.fitmart;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import lk.jiat.fitmart.model.BreadcrumbAdapter;
import lk.jiat.fitmart.model.BreadcrumbItem;
import lk.jiat.fitmart.model.Cart;
import lk.jiat.fitmart.model.CustomToast;
import lk.jiat.fitmart.model.Product;
import lk.jiat.fitmart.model.SuggesterProductsAdapter;

public class SingleProductDetailsActivity extends AppCompatActivity implements BreadcrumbAdapter.OnBreadcrumbItemClickListener {
    private RecyclerView breadcrumbRecyclerView;
    private BreadcrumbAdapter breadcrumbAdapter;
    private List<Product> productList;
    private Context context;
    private String price;

    private List<BreadcrumbItem> breadcrumbItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_single_product_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.button));

        context = this;

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String pid = intent.getStringExtra("productId");
        String description = intent.getStringExtra("description");
        price = intent.getStringExtra("price");
        String imageUrl = intent.getStringExtra("imageUrl");
        String delivery = intent.getStringExtra("deliveryfee");
        String qty = intent.getStringExtra("qty");
        String category = intent.getStringExtra("category");

        TextView titleView = findViewById(R.id.fnamec);
        TextView descView = findViewById(R.id.desc);
        TextView priceView = findViewById(R.id.experience);
        ImageView imageView = findViewById(R.id.imageView4);
        NumberPicker numberPicker = findViewById(R.id.numberPicker);
        EditText etQuantity = findViewById(R.id.etquantity);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Button cartbutton = findViewById(R.id.button4);
        Button buyNowbutton = findViewById(R.id.button5);

        int maxqty = Integer.parseInt(qty);

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(maxqty);

        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) ->
                etQuantity.setText(String.valueOf(newVal))
        );

        titleView.setText(title);
        descView.setText(description);
        priceView.setText("Rs. " + price + " .00");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(SingleProductDetailsActivity.this)
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.creatine) // Your placeholder image
                    .error(R.drawable.creatine)
                    .into(imageView);
        }

        if (title == null) {
            title = "Product Details";
        }

        int selectedQuantity = numberPicker.getValue();
        final int finalQuantity = selectedQuantity;

        String finalTitle1 = title;
        buyNowbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int finalQuantity = numberPicker.getValue();

                // Validate price
                if (price == null || price.isEmpty()) {
                    Toast.makeText(SingleProductDetailsActivity.this, "Invalid price", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Parse price and calculate total
                double intentPrice = Double.parseDouble(price);
                double totalAmount = intentPrice * finalQuantity;
                DecimalFormat df = new DecimalFormat("#0.00");
                String formattedTotal = df.format(totalAmount);

                // Start SingleBuyActivity
                Intent intent1 = new Intent(SingleProductDetailsActivity.this, SingleBuyActivity.class);
                intent1.putExtra("qty", finalQuantity);
                intent1.putExtra("price", price);
                intent1.putExtra("total", formattedTotal);
                intent1.putExtra("title", finalTitle1);
                startActivity(intent1);
            }
        });



        String finalTitle = title;
        cartbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
                String mobile = sharedPreferences.getString("mobile", "0714833745");

                int finalQuantity = numberPicker.getValue();


                int productIdInt;
                try {
                    productIdInt = Integer.parseInt(pid);
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Invalid product ID", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (!mobile.isEmpty()) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("cart")
                            .whereEqualTo("mobile", mobile)
                            .whereEqualTo("productId", productIdInt)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (querySnapshot.isEmpty()) {
                                    Cart cartItem = new Cart(
                                            productIdInt,
                                            finalTitle,
                                            price,
                                            imageUrl,
                                            finalQuantity,
                                            mobile,
                                            delivery
                                    );
                                    cartItem.setQuantity(finalQuantity); // Set the selected quantity

                                    db.collection("cart")
                                            .add(cartItem)
                                            .addOnSuccessListener(documentReference ->
                                                    runOnUiThread(() ->
                                                            CustomToast.show(context, "Product Added To the Cart", R.drawable.success)
                                                    )
                                            )
                                            .addOnFailureListener(e ->
                                                    runOnUiThread(() ->
                                                            Toast.makeText(context, "Failed to add to cart", Toast.LENGTH_SHORT).show()
                                                    )
                                            );
                                } else {

                                    DocumentSnapshot cartDoc = querySnapshot.getDocuments().get(0);
                                    Long currentQtyLong = cartDoc.getLong("quantity");
                                    int currentQty = (currentQtyLong != null) ? currentQtyLong.intValue() : 0;
                                    int newQuantity = currentQty + finalQuantity;

                                    // Check if new quantity exceeds maximum
                                    if (newQuantity > maxqty) {
                                        runOnUiThread(() ->
                                                Toast.makeText(context, "Cannot exceed maximum quantity of " + maxqty, Toast.LENGTH_SHORT).show()
                                        );
                                        return;
                                    }

                                    cartDoc.getReference()
                                            .update("quantity", newQuantity)
                                            .addOnSuccessListener(aVoid ->
                                                    runOnUiThread(() ->
                                                            Toast.makeText(context, "Cart updated", Toast.LENGTH_SHORT).show()
                                                    )
                                            )
                                            .addOnFailureListener(e ->
                                                    runOnUiThread(() ->
                                                            Toast.makeText(context, "Failed to update cart", Toast.LENGTH_SHORT).show()
                                                    )
                                            );
                                }
                            })
                            .addOnFailureListener(e ->
                                    runOnUiThread(() ->
                                            Toast.makeText(context, "Error checking cart", Toast.LENGTH_SHORT).show()
                                    )
                            );
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                    );
                }
            }

        });


        breadcrumbRecyclerView = findViewById(R.id.breadcrumb_recycler_view);
        breadcrumbRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        breadcrumbItems = new ArrayList<>();
        breadcrumbItems.add(new BreadcrumbItem("Home", false));
        breadcrumbItems.add(new BreadcrumbItem("Products", false));
        breadcrumbItems.add(new BreadcrumbItem(title, true));

        breadcrumbAdapter = new BreadcrumbAdapter(this, breadcrumbItems, this);
        breadcrumbRecyclerView.setAdapter(breadcrumbAdapter);

        productList = new ArrayList<>(); // Initialize productList before using it

        RecyclerView suggestedRecyclerView = findViewById(R.id.recycler_suggestedproducts);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL); // Ensure vertical orientation
        suggestedRecyclerView.setLayoutManager(layoutManager);

        int spacing = 32; // Adjust this value to increase/decrease spacing
        suggestedRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                int column = position % 2;

                outRect.left = column == 0 ? spacing : spacing/2;
                outRect.right = column == 1 ? spacing : spacing/2;
                outRect.top = spacing;
                outRect.bottom = spacing;
            }
        });

        suggestedRecyclerView.setHasFixedSize(true);

        SuggesterProductsAdapter adapter = new SuggesterProductsAdapter(this, productList);
        suggestedRecyclerView.setAdapter(adapter);

        firestore.collection("product")
                .whereEqualTo("category", category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            String imageUrl1 = documentSnapshot.getString("imageUrl");
                            if (imageUrl1 == null) {
                                imageUrl1 = documentSnapshot.getString("imagePath");
                            }

                            String title1 = documentSnapshot.getString("title");
                            String price1 = documentSnapshot.getString("price");

                            Product product = new Product(title1, price1, imageUrl1);
                            productList.add(product);
                        }
                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                        });

                    } else {
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failure", e));



    }

    @Override
    public void onBreadcrumbItemClick(int position) {

        BreadcrumbItem clickedItem = breadcrumbItems.get(position);

        for (int i = 0; i < breadcrumbItems.size(); i++) {
            BreadcrumbItem item = breadcrumbItems.get(i);
            breadcrumbItems.set(i, new BreadcrumbItem(item.getTitle(), i == position));
        }

        breadcrumbAdapter.notifyDataSetChanged();

        // Navigate to the appropriate screen based on position
        switch (position) {
            case 0: // Home
                Intent homeIntent = new Intent(this, HomeActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                finish();
                break;
            case 1: // Category
                onBackPressed();
                break;
            case 2: // Product
                break;
        }
    }

    public void updateBreadcrumb(String... paths) {
        breadcrumbItems.clear();
        for (int i = 0; i < paths.length; i++) {
            breadcrumbItems.add(new BreadcrumbItem(paths[i], i == paths.length - 1));
        }
        breadcrumbAdapter.notifyDataSetChanged();
    }
}