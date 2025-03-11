package lk.jiat.fitmart.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import lk.jiat.fitmart.R;

public class BreadcrumbAdapter extends RecyclerView.Adapter<BreadcrumbAdapter.ViewHolder> {
    private List<BreadcrumbItem> items;
    private Context context;
    private OnBreadcrumbItemClickListener listener;

    public interface OnBreadcrumbItemClickListener {
        void onBreadcrumbItemClick(int position);
    }

    public BreadcrumbAdapter(Context context, List<BreadcrumbItem> items, OnBreadcrumbItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.breadcrumb, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BreadcrumbItem item = items.get(position);

        holder.titleText.setText(item.getTitle());

        // Set text color based on active state
        int textColor = item.isActive() ?
                context.getResources().getColor(R.color.text) :
                context.getResources().getColor(R.color.button);
        holder.titleText.setTextColor(textColor);

        // Show separator except for last item
        holder.separator.setVisibility(position < items.size() - 1 ? View.VISIBLE : View.GONE);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBreadcrumbItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView separator;

        public ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.breadcrumb_title);
            separator = itemView.findViewById(R.id.breadcrumb_separator);
        }
    }
}