package lk.jiat.fitmart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import lk.jiat.fitmart.R;

public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.ViewHolder> {
    private List<Appointment> appointmentList;

    public AppointmentsAdapter(List<Appointment> appointmentList) {
        this.appointmentList = appointmentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);
        holder.trainerName.setText(appointment.getTrainer());
        holder.appointmentDate.setText("Date: " + appointment.getDate());
        holder.appointmentTime.setText("Time: " + appointment.getTime());
        holder.appointmentStatus.setText("Status: " + appointment.getStatus());
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView trainerName, appointmentDate, appointmentTime, appointmentStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            trainerName = itemView.findViewById(R.id.trainerName);
            appointmentDate = itemView.findViewById(R.id.appointmentDate);
            appointmentTime = itemView.findViewById(R.id.appointmentTime);
            appointmentStatus = itemView.findViewById(R.id.appointmentStatus);
        }
    }
}
