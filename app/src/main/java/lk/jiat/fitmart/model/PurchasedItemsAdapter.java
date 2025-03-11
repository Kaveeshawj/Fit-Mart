package lk.jiat.fitmart.model;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import lk.jiat.fitmart.R;

public class PurchasedItemsAdapter extends RecyclerView.Adapter<PurchasedItemsAdapter.PurchasedItemViewHolder> {

    private Context context;
    private List<Map<String, Object>> purchasedItems;

    public PurchasedItemsAdapter(Context context, List<Map<String, Object>> purchasedItems) {
        this.context = context;
        this.purchasedItems = purchasedItems;
    }

    @NonNull
    @Override
    public PurchasedItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_purchased_product, parent, false);
        return new PurchasedItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchasedItemViewHolder holder, int position) {
        Map<String, Object> item = purchasedItems.get(position);
        Log.i("MyOrders", "Binding item: " + item);

        holder.productTitle.setText(item.get("productName") != null ? String.valueOf(item.get("productName")) : "N/A");
        holder.productPrice.setText(item.get("price") != null ? String.format("Rs. %s", item.get("price")) : "N/A");

        if (item.get("quantity") instanceof Number) {
            int quantity = ((Number) item.get("quantity")).intValue();
            holder.productQty.setText(String.format("Qty: %d", quantity));
        } else {
            holder.productQty.setText("Qty: N/A");
        }

        holder.deliveryStatus.setText(item.get("status") != null ? String.format("Delivery Status: %s", item.get("status")) : "N/A");

        String status = String.valueOf(item.get("status"));
        if ("Delivered".equals(status)) {
            holder.deliveryStatus.setTextColor(Color.GREEN);
        } else if ("In Transit".equals(status)) {
            holder.deliveryStatus.setTextColor(Color.BLUE);
        } else {
            holder.deliveryStatus.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return purchasedItems.size();
    }

    static class PurchasedItemViewHolder extends RecyclerView.ViewHolder {
        TextView productTitle, productPrice, productQty, deliveryStatus;

        public PurchasedItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productTitle = itemView.findViewById(R.id.productTitle);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQty = itemView.findViewById(R.id.productQty);
            deliveryStatus = itemView.findViewById(R.id.deliveryStatus);
        }
    }
}
