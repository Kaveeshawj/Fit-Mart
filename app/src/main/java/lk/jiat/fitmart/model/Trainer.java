package lk.jiat.fitmart.model;

public class Trainer {

    private String id;
    private String name;
    private String specialization;
    private String experience;
    private String imageUrl;
    private String price;
    private String gender;

    private String mobile;
    private String status;

    public Trainer(String id, String name, String specialization, String experience, String imageUrl, String price, String gender, String mobile, String status) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.experience = experience;
        this.imageUrl = imageUrl;
        this.price = price;
        this.gender = gender;
        this.mobile = mobile;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
