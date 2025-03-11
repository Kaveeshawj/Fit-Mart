package lk.jiat.fitmart.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import lk.jiat.fitmart.R;

public class updateCartBadge {
    private Context context;
    private TextView cartBadge;
    private FirebaseFirestore db;
    private String mobile;

    public updateCartBadge(Context context, TextView cartBadge, String mobile) {
        this.context = context;
        this.cartBadge = cartBadge;
        this.mobile = mobile;
        this.db = FirebaseFirestore.getInstance();
        setupCartListener();
    }

    private void setupCartListener() {
        // Direct path to the cart collection based on your Firebase structure
        db.collection("cart")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        if (snapshots != null && !snapshots.isEmpty()) {
                            int totalQuantity = 0;
                            for (var document : snapshots.getDocuments()) {
                                // Check if this cart item belongs to the current user
                                if (document.getString("mobile") != null &&
                                        document.getString("mobile").equals(mobile)) {
                                    Long quantity = document.getLong("quantity");
                                    if (quantity != null) {
                                        totalQuantity += quantity.intValue();
                                    }
                                }
                            }
                            updateBadgeCount(totalQuantity);
                        } else {
                            updateBadgeCount(0);
                        }
                    }
                });
    }

    private void updateBadgeCount(int count) {
        if (cartBadge != null) {
            if (count > 0) {
                cartBadge.setVisibility(View.VISIBLE);
                cartBadge.setText(count > 10 ? "10+" : String.valueOf(count));
            } else {
                cartBadge.setVisibility(View.GONE);
            }
        }
    }
}