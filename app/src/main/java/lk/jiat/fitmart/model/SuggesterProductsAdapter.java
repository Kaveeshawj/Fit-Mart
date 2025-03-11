package lk.jiat.fitmart.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import lk.jiat.fitmart.R;

public class SuggesterProductsAdapter extends RecyclerView.Adapter<SuggesterProductsAdapter.ViewHolder> {
    private List<Product> productList;
    private Context context;

    public SuggesterProductsAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    public void updateData(List<Product> newProductList) {
        productList.clear();
        productList.addAll(newProductList);
        notifyDataSetChanged(); // Notify the adapter of data changes
    }

    @NonNull
    @Override
    public SuggesterProductsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new SuggesterProductsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productName.setText(product.getTitle());
        holder.productPrice.setText("Rs. " + product.getPrice());


        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.creatine)
                .error(R.drawable.creatine)
                .into(holder.productImage);
    }


    @Override
    public int getItemCount() {
        return productList.size();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice;
        ImageView productImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name1);
            productPrice = itemView.findViewById(R.id.product_price1);
            productImage = itemView.findViewById(R.id.image1);
        }
    }
}
