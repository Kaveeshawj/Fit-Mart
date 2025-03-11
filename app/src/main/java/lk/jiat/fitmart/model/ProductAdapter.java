package lk.jiat.fitmart.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.fitmart.R;
import lk.jiat.fitmart.SingleProductDetailsActivity;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> productList;
    private Context context;
    private static final String TAG = "MainActivity";

    public interface OnProductClickListener {
        void onBuyNowClick(Product product);
    }

    private OnProductClickListener listener;

    public ProductAdapter(Context context, List<Product> productList) {

        Log.d(TAG, "ProductAdapter constructor called with " +
                (productList != null ? productList.size() : 0) + " items");
        this.context = context;
        this.productList = productList != null ? productList : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Log.d(TAG, "onCreateViewHolder called");
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_product_recycler, parent, false);
            return new ViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateViewHolder", e);
            throw e;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            Product product = productList.get(position);

            if (holder.brandName != null) {
                holder.brandName.setText(product.getCategory());
            }

            if (holder.titlePro != null) {
                holder.titlePro.setText(product.getTitle());
            }

            if (holder.price != null) {
                String priceStr = product.getPrice().replaceAll("[^\\d.]", "");
                try {
                    double priceValue = Double.parseDouble(priceStr);
                    holder.price.setText(String.format("Rs. %.2f", priceValue));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing price: " + product.getPrice(), e);
                    holder.price.setText("Rs. " + product.getPrice());
                }
            }

            if (holder.productImage != null) {
                String imageUrl = product.getImageUrl().toString(); // Assuming you've added getImageUrl() to Product class
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(context)
                            .load(imageUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.creatine) // Your placeholder image
                            .error(R.drawable.creatine) // Your error image
                            .into(holder.productImage);
                    Log.d(TAG, "Loading image from URL: " + imageUrl);
                } else {
                    Log.w(TAG, "No image URL available for product at position " + position);
                    holder.productImage.setImageResource(R.drawable.creatine);
                }
            }


            if (holder.cartButton != null) {
                holder.cartButton.setOnClickListener(v -> {
                    SharedPreferences sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                    String mobile = sharedPreferences.getString("mobile", "0714833745");

                    Log.d("TAG12232", "Final delivery fee: " + product.getTitle());
                    Log.d("TAG12232", "Final delivery fee: " + product.getDeliveryfee());

                    if (!mobile.isEmpty()) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        db.collection("cart")
                                .whereEqualTo("mobile", mobile)
                                .whereEqualTo("productId", product.getId())
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().isEmpty()) {

                                            Cart cartItem = new Cart(
                                                    product.getId(),
                                                    product.getTitle(),
                                                    product.getPrice(),
                                                    product.getImageUrl(),
                                                    1,
                                                    mobile,
                                                    product.getDeliveryfee()
                                            );

                                            db.collection("cart")
                                                    .add(cartItem)
                                                    .addOnSuccessListener(documentReference -> {
                                                        CustomToast.show(context,"Product Added To the Cart",R.drawable.success);

                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(context, "Failed to add to cart", Toast.LENGTH_SHORT).show();

                                                    });
                                        } else {
                                            DocumentSnapshot cartDoc = task.getResult().getDocuments().get(0);

                                            // Get the quantity correctly as an integer
                                            Long currentQtyLong = cartDoc.getLong("quantity");
                                            int currentQty = (currentQtyLong != null) ? currentQtyLong.intValue() : 1;

                                            int newQuantity = currentQty + 1;
                                            cartDoc.getReference()
                                                    .update("quantity", newQuantity) // Store as integer
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(context, "Cart updated", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(context, "Failed to update cart", Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            holder.buynowButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, SingleProductDetailsActivity.class); // Create your ProductDetailsActivity


                String productId = String.valueOf(product.getId());
                // Pass all product details through intent
                intent.putExtra("productId", productId);
                intent.putExtra("title", product.getTitle());
                intent.putExtra("description", product.getDescription());
                intent.putExtra("price", product.getPrice());
                intent.putExtra("category", product.getCategory());
                intent.putExtra("imageUrl", product.getImageUrl());
                intent.putExtra("qty", product.getQty());
                intent.putExtra("deliveryfee", product.getDeliveryfee());


                Log.i(TAG, String.valueOf(product.getId()));

                context.startActivity(intent);
            });


        } catch (Exception e) {
            Log.e(TAG, "Error in onBindViewHolder at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView productImage;
        public TextView brandName;
        public TextView titlePro;
        public TextView price;
        public ImageButton cartButton;
        public ImageButton buynowButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                productImage = itemView.findViewById(R.id.productImage);
                brandName = itemView.findViewById(R.id.brandName);
                titlePro = itemView.findViewById(R.id.titlepro);
                price = itemView.findViewById(R.id.experience);
                cartButton = itemView.findViewById(R.id.cartButton);
                buynowButton = itemView.findViewById(R.id.buynowbtn);
            } catch (Exception e) {
                Log.e(TAG, "Error initializing ViewHolder views", e);
            }
        }
    }
}

