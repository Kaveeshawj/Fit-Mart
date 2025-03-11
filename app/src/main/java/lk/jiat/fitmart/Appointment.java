package lk.jiat.fitmart;

public class Appointment {
    private String trainer;
    private String date;
    private String time;
    private String status;

    public Appointment(String trainer, String date, String time, String status) {
        this.trainer = trainer;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public String getTrainer() {
        return trainer;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}
