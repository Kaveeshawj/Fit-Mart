package lk.jiat.fitmart.navigations;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import lk.jiat.fitmart.R;
import lk.jiat.fitmart.model.Product;
import lk.jiat.fitmart.model.ProductAdapter;
public class ShopFragment extends Fragment {
    private static final String TAG = "MainActivity";
    private List<Product> productList;
    private ProductAdapter productAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView started");
        try {
            // Verify the layout resource exists
            if (getResources().getLayout(R.layout.fragment_shop) == null) {
                Log.e(TAG, "fragment_shop layout not found");
                return null;
            }

            View view = inflater.inflate(R.layout.fragment_shop, container, false);
            Log.d(TAG, "Layout inflated successfully");
            return view;
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Resource not found error", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreateView", e);
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated started");


        try {
            RecyclerView recyclerView = view.findViewById(R.id.recycler_products);
            if (recyclerView == null) {
                Log.e(TAG, "RecyclerView not found in layout");
                return;
            }

            GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setPadding(0, 0, 0, 0);
            recyclerView.setClipToPadding(true);

            productList = new ArrayList<>();
            productAdapter = new ProductAdapter(requireContext(), productList);
            recyclerView.setAdapter(productAdapter);

            productDetails();

        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
        }
    }

    private void productDetails(){
        FirebaseFirestore firestore=FirebaseFirestore.getInstance();

        firestore.collection("product").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if ((task.isSuccessful())) {

                            productList.clear();

                            for (QueryDocumentSnapshot documentSnapshot:task.getResult()){

                                Date datetime = null;
                                if (documentSnapshot.exists() && documentSnapshot.contains("datetime")) {
                                    Timestamp timestamp = documentSnapshot.getTimestamp("datetime");
                                    if (timestamp != null) {
                                         datetime = timestamp.toDate();
                                    }
                                }


                                String imageUrl = documentSnapshot.getString("imageUrl");
                                if (imageUrl == null) {
                                    imageUrl = documentSnapshot.getString("imagePath");
                                }
                                Product product=new Product(
                                        documentSnapshot.getId().hashCode(),
                                        documentSnapshot.getString("title"),
                                        documentSnapshot.getString("description"),
                                        documentSnapshot.getString("price"),
                                        documentSnapshot.getString("category"),
                                        datetime,
                                        documentSnapshot.getString("statusId"),
                                        documentSnapshot.getString("deliveryfee"),
                                        documentSnapshot.getString("qty"),
                                        imageUrl
                                );

                                productList.add(product);
                            }
                        }
                        productAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error loading products", e);

                    }
                })
        ;
    }
}