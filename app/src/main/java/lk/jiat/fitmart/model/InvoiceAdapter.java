package lk.jiat.fitmart.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

import lk.jiat.fitmart.R;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {

    private Context context;
    private List<?> purchasedItems; // Generic list to support both types

    // Single constructor for both types
    public InvoiceAdapter(Context context, List<?> purchasedItems) {
        this.context = context;
        this.purchasedItems = purchasedItems;
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_invoice, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        if (purchasedItems.get(0) instanceof Map) {
            // Handle List<Map<String, String>>
            @SuppressWarnings("unchecked")
            Map<String, String> item = (Map<String, String>) purchasedItems.get(position);
            holder.productName.setText(item.get("title"));
            holder.productPrice.setText(String.format("Rs. %s", item.get("price")));
            holder.productQuantity.setText(String.format("Qty: %s", item.get("qty")));
        } else if (purchasedItems.get(0) instanceof Cart) {
            // Handle List<Cart>
            Cart item = (Cart) purchasedItems.get(position);
            holder.productName.setText(item.getTitle());
            holder.productPrice.setText(String.format("Rs. %s", item.getPrice()));
            holder.productQuantity.setText(String.format("Qty: %d", item.getQuantity()));
        }
    }

    @Override
    public int getItemCount() {
        return purchasedItems.size();
    }

    static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productQuantity;

        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQuantity = itemView.findViewById(R.id.productQuantity);
        }
    }
}