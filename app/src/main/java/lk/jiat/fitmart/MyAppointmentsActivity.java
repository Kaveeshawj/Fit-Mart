package lk.jiat.fitmart;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ShareActionProvider;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyAppointmentsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AppointmentsAdapter adapter;
    private List<Appointment> appointmentList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_appointments);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.button));

        recyclerView = findViewById(R.id.appointmentsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentList = new ArrayList<>();
        adapter = new AppointmentsAdapter(appointmentList);
        recyclerView.setAdapter(adapter);

        fetchAppointments();
    }

    private void fetchAppointments() {

        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        String mobile = sharedPreferences.getString("mobile","0714833745");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("bookings")
                .whereEqualTo("userId", mobile)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    appointmentList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String trainer = document.getString("trainer");
                        String date = document.getString("date");
                        String time = document.getString("time");
                        String status = document.getString("status");
                        appointmentList.add(new Appointment(trainer, date, time, status));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                });
    }

}