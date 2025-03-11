package lk.jiat.fitmart.model;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lk.jiat.fitmart.R;

public class MyOrdersFragment extends Fragment {
    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private PurchasedItemsAdapter adapter;
    private List<Map<String, Object>> purchasedItems = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_orders, container, false);

        firestore = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.purchasedItemsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new PurchasedItemsAdapter(requireContext(), purchasedItems);
        recyclerView.setAdapter(adapter);

        fetchPurchasedItems();

        return view;
    }

    private void fetchPurchasedItems() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPreferences", MODE_PRIVATE);
        String userMobile = sharedPreferences.getString("mobile", "0714833745");

        firestore.collection("invoices")
                .whereEqualTo("customerMobile", userMobile)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    purchasedItems.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> invoiceData = document.getData();
                        if (invoiceData != null && invoiceData.containsKey("items")) {
                            purchasedItems.addAll((List<Map<String, Object>>) invoiceData.get("items"));
                        }
                    }
                    requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

                    View emptyView = getView().findViewById(R.id.emptyView);
                    if (emptyView != null) {
                        emptyView.setVisibility(purchasedItems.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error fetching purchased items: " + e.getMessage());
                });
    }
}