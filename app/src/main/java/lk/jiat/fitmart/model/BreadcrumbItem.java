package lk.jiat.fitmart.model;


public class BreadcrumbItem {
    private String title;
    private boolean isActive;

    public BreadcrumbItem(String title, boolean isActive) {
        this.title = title;
        this.isActive = isActive;
    }

    public String getTitle() {
        return title;
    }

    public boolean isActive() {
        return isActive;
    }
}

