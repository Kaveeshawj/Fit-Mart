package lk.jiat.fitmart;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import lk.jiat.fitmart.model.CustomToast;
import lk.jiat.fitmart.model.NotificationReceiver;

public class BookTrainerActivity extends AppCompatActivity {
    private String selectedTime = "Not Set";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_trainer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        String name = getIntent().getStringExtra("name");
        String specialization = getIntent().getStringExtra("specialization");
        String experience = getIntent().getStringExtra("experience");
        String price = getIntent().getStringExtra("price");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String mobile = getIntent().getStringExtra("mobile");

        TextView nameTextView = findViewById(R.id.trainerName);
        TextView specializationTextView = findViewById(R.id.trainerSpecialization);
        TextView experienceTextView = findViewById(R.id.trainerExperience);
        TextView priceTextView = findViewById(R.id.trainerPrice);

        nameTextView.setText(name);
        specializationTextView.setText(specialization);
        experienceTextView.setText(experience + " Years");
        priceTextView.setText("Fee: Rs." + price + ".00 per hour");

        ImageView trainerImageView = findViewById(R.id.trainerImage);
        Glide.with(this).load(imageUrl).into(trainerImageView);

        Button selectTimeButton = findViewById(R.id.selectTimeButton);
        TextView selectedTimeTextView = findViewById(R.id.selectedTimeTextView);

        selectTimeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    BookTrainerActivity.this,
                    (view, hourOfDay, minuteOfHour) -> {
                        // Format the selected time
                        String formattedTime = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                        selectedTime = formattedTime; // Save the selected time
                        selectedTimeTextView.setText("Selected Time: " + formattedTime); // Display it
                    },
                    hour, minute, true
            );
            timePickerDialog.show();
        });


        Button checkAvailabilityButton = findViewById(R.id.checkAvailabilityButton);
        TextView trainerAvailabilityStatus = findViewById(R.id.trainerAvailabilityStatus);
        Button confirmBookingButton = findViewById(R.id.confirmBookingButton);

        checkAvailabilityButton.setOnClickListener(v -> {
            CalendarView calendarView = findViewById(R.id.calendarView);
            long selectedDateInMillis = calendarView.getDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String selectedDate = dateFormat.format(new Date(selectedDateInMillis));

            if ("Not Set".equals(selectedTime)) {
                CustomToast.show(BookTrainerActivity.this, "Please select a time first!", R.drawable.warning);
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("bookings")
                    .whereEqualTo("trainer", getIntent().getStringExtra("name"))
                    .whereEqualTo("date", selectedDate)
                    .whereEqualTo("time", selectedTime)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            String status = document.getString("status");
                            trainerAvailabilityStatus.setText("Availability: " + status);

                            if ("Available".equalsIgnoreCase(status)) {
                                confirmBookingButton.setEnabled(true); // Enable the button
                                trainerAvailabilityStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            } else {
                                confirmBookingButton.setEnabled(false); // Disable the button
                                trainerAvailabilityStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            }
                        } else {
                            trainerAvailabilityStatus.setText("Availability: Available");
                            confirmBookingButton.setEnabled(true); // Enable the button if no bookings exist
                            trainerAvailabilityStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        }
                    })
                    .addOnFailureListener(e -> {
                        trainerAvailabilityStatus.setText("Availability: Error Checking");
                        confirmBookingButton.setEnabled(false); // Disable the button in case of error
                        trainerAvailabilityStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    });
        });
        Button call=findViewById(R.id.callTrainerButton);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + mobile));
                startActivity(callIntent);
            }
        });


        confirmBookingButton.setOnClickListener(v -> {
            EditText additionalNotesEditText = findViewById(R.id.additionalNotes);
            String notes = additionalNotesEditText.getText().toString();

            new AlertDialog.Builder(BookTrainerActivity.this)
                    .setTitle("Confirm Booking")
                    .setMessage("Are you sure you want to book this appointment?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        saveBookingToFirestore(notes);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void saveBookingToFirestore(String notes) {
        CalendarView calendarView = findViewById(R.id.calendarView);
        long selectedDateInMillis = calendarView.getDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String selectedDate = dateFormat.format(new Date(selectedDateInMillis));
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        String mobile = sharedPreferences.getString("mobile", "0714833745");


        HashMap<String, Object> bookingMap = new HashMap<>();
        bookingMap.put("trainer", getIntent().getStringExtra("name"));
        bookingMap.put("userId", mobile);
        bookingMap.put("date", selectedDate);
        bookingMap.put("time", selectedTime);
        bookingMap.put("notes", notes);
        bookingMap.put("status", "Pending");

        FirebaseFirestore.getInstance().collection("bookings")
                .add(bookingMap)
                .addOnSuccessListener(documentReference -> {
                    CustomToast.show(BookTrainerActivity.this, "Booking Confirmed!", R.drawable.success);
                    scheduleNotification(selectedDate, selectedTime);
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> {
                    CustomToast.show(BookTrainerActivity.this, "Booking Failed", R.drawable.error);
                });
    }


    private void scheduleNotification(String selectedDate, String selectedTime) {
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date appointmentDateTime = dateTimeFormat.parse(selectedDate + " " + selectedTime);
            calendar.setTime(appointmentDateTime);

            calendar.add(Calendar.MINUTE, -10);

            Calendar now = Calendar.getInstance();
            if (calendar.before(now)) {
                CustomToast.show(BookTrainerActivity.this, "Please select a time at least 10 minutes ahead!", R.drawable.warning);
                return;
            }

            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("title", "Appointment Reminder");
            intent.putExtra("message", "Your appointment is in 10 minutes!");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}