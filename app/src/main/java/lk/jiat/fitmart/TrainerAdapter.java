package lk.jiat.fitmart;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import lk.jiat.fitmart.model.Trainer;

public class TrainerAdapter extends RecyclerView.Adapter<TrainerAdapter.ViewHolder> {

    private Context context;
    private List<Trainer> trainerList;

    public TrainerAdapter(Context context, List<Trainer> trainerList) {
        this.context = context;
        this.trainerList = trainerList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.appointments_recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trainer trainer = trainerList.get(position);
        holder.name.setText(trainer.getName());
        holder.specialization.setText(trainer.getSpecialization());
        holder.experience.setText(trainer.getExperience() + " Years");
        holder.price.setText("Fee: Rs." + trainer.getPrice() +".00 per hour");

        Glide.with(context).load(trainer.getImageUrl()).into(holder.profileImage);


        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BookTrainerActivity.class);
                intent.putExtra("name", trainer.getName());
                intent.putExtra("specialization", trainer.getSpecialization());
                intent.putExtra("experience", trainer.getExperience());
                intent.putExtra("price", trainer.getPrice());
                intent.putExtra("imageUrl", trainer.getImageUrl());
                intent.putExtra("mobile", trainer.getMobile());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trainerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, specialization, experience, languages, price;
        ImageView profileImage;
        Button button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.trainer_name);
            specialization = itemView.findViewById(R.id.trainer_specialization);
            experience = itemView.findViewById(R.id.trainer_experience);
            price = itemView.findViewById(R.id.trainer_price);
            profileImage = itemView.findViewById(R.id.trainer_image);
            button=itemView.findViewById(R.id.btnBook);
        }
    }
}

