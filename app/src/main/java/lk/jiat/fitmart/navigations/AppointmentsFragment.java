package lk.jiat.fitmart.navigations;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

import lk.jiat.fitmart.MyAppointmentsActivity;
import lk.jiat.fitmart.R;
import lk.jiat.fitmart.TrainerAdapter;
import lk.jiat.fitmart.model.Trainer;

public class AppointmentsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TrainerAdapter trainerAdapter;
    private List<Trainer> trainerList;
    private Context context;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        Button button=view.findViewById(R.id.textView7);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(requireContext(), MyAppointmentsActivity.class);
                startActivity(intent);
            }
        });

        // Initialize trainer list
        trainerList = new ArrayList<>();

        // Set up the adapter
        trainerAdapter = new TrainerAdapter(requireContext(), trainerList);
        recyclerView.setAdapter(trainerAdapter);

        // Fetch trainers from Firebase Firestore
        fetchTrainersFromFirebase();

        return view;
    }

    private void fetchTrainersFromFirebase() {
        FirebaseFirestore.getInstance().collection("trainer")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Clear the existing list
                    trainerList.clear();

                    // Iterate through the documents and add trainers to the list
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getId();
                        String name = document.getString("fname") + " " + document.getString("lname");
                        String specialization = document.getString("speiality");
                        String experience = document.getString("experience");
                        String imageUrl = document.getString("imageUrl");
                        String price = document.getString("fee");
                        String gender = document.getString("gender");
                        String mobile = document.getString("mobile");
                        String status = document.getString("status");

                        Trainer trainer = new Trainer(
                                id,
                                name,
                                specialization,
                                experience,
                                imageUrl,
                                price,
                                gender,
                                mobile,
                                status
                        );
                        trainerList.add(trainer);
                    }

                    // Notify the adapter of data changes
                    trainerAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    System.out.println("Error fetching trainers: " + e.getMessage());
                });
    }
}