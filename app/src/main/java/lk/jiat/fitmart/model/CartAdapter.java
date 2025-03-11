package lk.jiat.fitmart.model;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import lk.jiat.fitmart.R;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<Cart> cartItems;
    private CartItemClickListener listener;

    public CartAdapter(Context context, List<Cart> cartItems, CartItemClickListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext(); // Initialize context if not already set
        View view = LayoutInflater.from(context)
                .inflate(R.layout.cart_recycler_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Cart item = cartItems.get(position);

        holder.cartTitle.setText(item.getTitle());
        holder.cartQty.setText(String.valueOf(item.getQuantity()));

        // Price formatting
        if (holder.cartPrice != null) {
            String priceStr = item.getPrice().replaceAll("[^\\d.]", "");
            try {
                double priceValue = Double.parseDouble(priceStr);
                holder.cartPrice.setText(String.format("Rs. %.2f", priceValue));
            } catch (NumberFormatException e) {
                Log.e("CartAdapter", "Error parsing price: " + item.getPrice(), e);
                holder.cartPrice.setText("Rs. " + item.getPrice());
            }
        }

        // Image loading
        if (holder.productImage != null) {
            String imageUrl = item.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.creatine)
                        .error(R.drawable.creatine)
                        .into(holder.productImage);
                Log.d("CartAdapter", "Loading image from URL: " + imageUrl);
            } else {
                Log.w("CartAdapter", "No image URL available for product at position " + position);
                holder.productImage.setImageResource(R.drawable.creatine);
            }
        }

        // Delete button click handler
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public void updateCartItems(List<Cart> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageButton deleteButton;
        TextView cartTitle;
        TextView cartQty;
        TextView cartPrice;
        ImageView productImage;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            deleteButton = itemView.findViewById(R.id.imageButton3);
            cartTitle = itemView.findViewById(R.id.cartTitle);
            cartQty = itemView.findViewById(R.id.cartQty);
            cartPrice = itemView.findViewById(R.id.cartPrice);
            productImage = itemView.findViewById(R.id.imageView6);
        }
    }

    public interface CartItemClickListener {
        void onDeleteClick(int position);
    }
}