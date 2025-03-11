package lk.jiat.fitmart.model;

import android.util.Log;

import java.io.Serializable;

public class Cart implements Serializable {
    private int productId;
    private String title;
    private String price;
    private String imageUrl;
    private int quantity;
    private String mobile;
    private String deliveryfee;

    public Cart() {
    }

    public Cart(int productId, String title, String price, String imageUrl, int quantity, String mobile, String deliveryfee)  {
        this.productId = productId;
        this.title = title;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.mobile = mobile;
        Log.d("TAG12232", "Incoming delivery fee: " + deliveryfee);
        this.deliveryfee = deliveryfee != null && !deliveryfee.isEmpty() ? deliveryfee : "0";
        Log.d("TAG12232", "Final delivery fee: " + this.deliveryfee);
        this.deliveryfee = deliveryfee != null ? deliveryfee : "0";
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getDeliveryfee() {
        return deliveryfee;
    }

    public void setDeliveryfee(String deliveryfee) {
        this.deliveryfee = deliveryfee;
    }
}
