package lk.jiat.fitmart.navigations;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.fitmart.BuyActivity;
import lk.jiat.fitmart.R;
import lk.jiat.fitmart.model.Cart;
import lk.jiat.fitmart.model.CartAdapter;

public class CartFragment extends Fragment implements CartAdapter.CartItemClickListener {
    private static final String TAG = "CartFragment";
    private List<Cart> cartList;
    private CartAdapter cartAdapter;
    private FirebaseFirestore firestore;
    private String userMobile;

    private  double total;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button button = view.findViewById(R.id.button6);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String totalAmount = String.valueOf(total);

                Intent intent=new Intent(requireContext(), BuyActivity.class);
                intent.putExtra("TOTAL_KEY",totalAmount);

                startActivity(intent);
            }
        });


        firestore = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = requireActivity()
                .getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        userMobile = sharedPreferences.getString("mobile", "0714833745");

        try {
            RecyclerView recyclerView = view.findViewById(R.id.cartItemsRecyclerView);
            if (recyclerView == null) {
                Log.e(TAG, "RecyclerView not found in layout");
                return;
            }

            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
            recyclerView.setLayoutManager(layoutManager);

            cartList = new ArrayList<>();
            cartAdapter = new CartAdapter(requireContext(), cartList, this);
            recyclerView.setAdapter(cartAdapter);

            fetchCartItems();

        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
        }
    }

    private void fetchCartItems() {
        firestore.collection("cart")
                .whereEqualTo("mobile", userMobile)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cartList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Cart cart = new Cart(
                                        document.getLong("productId").intValue(),
                                        document.getString("title"),
                                        document.getString("price"),
                                        document.getString("imageUrl"),
                                        document.getLong("quantity").intValue(),
                                        document.getString("mobile"),
                                        document.getString("deliveryfee")
                                );
                                cartList.add(cart);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing cart item: " + document.getId(), e);
                            }
                        }
                        cartAdapter.notifyDataSetChanged();
                        calculatePrices();
                    } else {
                        Log.e(TAG, "Error getting cart items", task.getException());
                    }
                });
    }

    private void calculatePrices() {
        double subtotal = 0;
        double totalShipping = 0;

        // Calculate subtotal and shipping separately
        for (Cart item : cartList) {
            try {
                // Calculate item price
                String priceStr = item.getPrice().replaceAll("[^\\d.]", "");
                double price = Double.parseDouble(priceStr);
                subtotal += (price * item.getQuantity());

                // Calculate shipping for each item
                String shippingStr = item.getDeliveryfee().replaceAll("[^\\d.]", "");
                double itemShipping = Double.parseDouble(shippingStr);
                totalShipping += itemShipping;
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing price or shipping: " + item.getPrice(), e);
            }
        }

        // Calculate total
        total = subtotal + totalShipping;

        // Update UI
        updatePriceDisplays(subtotal, totalShipping, total);
    }

    private void updatePriceDisplays(double subtotal, double shipping, double total) {
        View view = getView();
        if (view != null) {
            // Update subtotal
            TextView subtotalText = view.findViewById(R.id.subtotalAmount);
            if (subtotalText != null) {
                subtotalText.setText(String.format("Rs. %.2f", subtotal));
            }

            // Update shipping
            TextView shippingText = view.findViewById(R.id.shippingAmount);
            if (shippingText != null) {
                shippingText.setText(String.format("Rs. %.2f", shipping));
            }

            // Update total
            TextView totalText = view.findViewById(R.id.totalAmount);
            if (totalText != null) {
                totalText.setText(String.format("Rs. %.2f", total));
            }
        }
    }

    @Override
    public void onDeleteClick(int position) {
        if (position >= 0 && position < cartList.size()) {
            Cart cartItem = cartList.get(position);
            firestore.collection("cart")
                    .whereEqualTo("mobile", userMobile)
                    .whereEqualTo("productId", cartItem.getProductId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        cartList.remove(position);
                                        cartAdapter.notifyItemRemoved(position);
                                        calculatePrices();
                                    })
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Error deleting cart item", e));
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error finding cart item to delete", e));
        }
    }


}