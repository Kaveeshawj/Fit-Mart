package lk.jiat.fitmart;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.jiat.fitmart.model.Cart;
import lk.jiat.fitmart.model.CustomToast;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;

public class BuyActivity extends AppCompatActivity {
    private String documentId;
    private static final int PAYHERE_REQUEST_CODE = 1001;

    private String Usermobile;
    private String userEmail;

    private TextInputEditText fnameEditText;
    private TextInputEditText lnameEditText;
    private TextInputEditText mobileEditText;
    private TextInputEditText line1EditText;
    private TextInputEditText line2EditText;
    private TextInputEditText cityEditText;

    private FirebaseFirestore firestore;
    private static final String MERCHANT_ID = "1221623";
    private static final String MERCHANT_SECRET = "MTkwODEwMzQ5OTAwNDAxNzUyNDE3NTIwNzcyNDgzOTk4NDY2NQ==";

    private List<Cart> purchasedItems = new ArrayList<>();

    private final ActivityResultLauncher<Intent> payHereLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                        Serializable serializable = data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
                        if (serializable instanceof PHResponse) {
                            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) serializable;
                            if (response.isSuccess()) {
                                // Payment successful, navigate to InvoiceActivity
                                Intent invoiceIntent = new Intent(this, InvoiceActivity.class);

                                // Pass purchased items to the InvoiceActivity
                                invoiceIntent.putExtra("PURCHASED_ITEMS", (Serializable) purchasedItems);

                                // Add other details to the intent
                                invoiceIntent.putExtra("ORDER_ID", generateUniqueOrderId());
                                invoiceIntent.putExtra("CUSTOMER_NAME", fnameEditText.getText().toString() + " " + lnameEditText.getText().toString());
                                invoiceIntent.putExtra("CUSTOMER_EMAIL", userEmail != null ? userEmail : "customer@example.com");
                                invoiceIntent.putExtra("AMOUNT", data.getStringExtra("TOTAL_KEY"));

                                // Start the InvoiceActivity
                                startActivity(invoiceIntent);
                            } else {
                                // Payment failed
                                Toast.makeText(this, "Payment Failed: " + response.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Log.d("PayHere", "User Cancelled");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buy);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String totalInput = intent.getStringExtra("TOTAL_KEY");

        double totalAmount = Double.parseDouble(totalInput);
        DecimalFormat df = new DecimalFormat("#0.00");
        final String formattedTotal = df.format(totalAmount);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        Usermobile = sharedPreferences.getString("mobile", "0714833745");

        CheckBox checkBox = findViewById(R.id.checkBox);

        TextView textView = findViewById(R.id.totalAmount);
        textView.setText("Rs. " + formattedTotal);

        fnameEditText = findViewById(R.id.fnamec);
        lnameEditText = findViewById(R.id.lnamec);
        mobileEditText = findViewById(R.id.experience);
        line1EditText = findViewById(R.id.fees);
        line2EditText = findViewById(R.id.line2c);
        cityEditText = findViewById(R.id.cityc);
        TextInputEditText postalEditText = findViewById(R.id.tline1);

        Button button = findViewById(R.id.button3);

        firestore = FirebaseFirestore.getInstance();

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && Usermobile != null) {
                firestore.collection("users")
                        .whereEqualTo("mobile", Usermobile)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                documentId = document.getId();

                                Map<String, Object> userData = document.getData();

                                fnameEditText.setText(userData.get("fname") != null ? String.valueOf(userData.get("fname")) : "");
                                lnameEditText.setText(userData.get("lname") != null ? String.valueOf(userData.get("lname")) : "");
                                mobileEditText.setText(userData.get("mobile") != null ? String.valueOf(userData.get("mobile")) : "");
                                line1EditText.setText(userData.get("line1") != null ? String.valueOf(userData.get("line1")) : "");
                                line2EditText.setText(userData.get("line2") != null ? String.valueOf(userData.get("line2")) : "");
                                cityEditText.setText(userData.get("city") != null ? String.valueOf(userData.get("city")) : "");
                                postalEditText.setText(userData.get("postal") != null ? String.valueOf(userData.get("postal")) : "");

                                userEmail = userData.get("email") != null ? String.valueOf(userData.get("email")) : "";
                            }
                        })
                        .addOnFailureListener(e -> Log.e("TAG", "Error loading user data", e));
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fname = fnameEditText.getText().toString();
                String lname = lnameEditText.getText().toString();
                String mobile = mobileEditText.getText().toString();
                String line1 = line1EditText.getText().toString();
                String line2 = line2EditText.getText().toString();
                String city = cityEditText.getText().toString();
                String postal = postalEditText.getText().toString();

                if (TextUtils.isEmpty(fname)) {
                    CustomToast.show(BuyActivity.this, "Please enter the First Name", R.drawable.error);
                } else if (TextUtils.isEmpty(lname)) {
                    CustomToast.show(BuyActivity.this, "Please enter the Last Name", R.drawable.error);
                } else if (TextUtils.isEmpty(mobile)) {
                    CustomToast.show(BuyActivity.this, "Please enter the Mobile", R.drawable.error);
                } else if (TextUtils.isEmpty(line1)) {
                    CustomToast.show(BuyActivity.this, "Please enter the Address Line 1", R.drawable.error);
                } else if (TextUtils.isEmpty(line2)) {
                    CustomToast.show(BuyActivity.this, "Please enter the Address Line 2", R.drawable.error);
                } else if (TextUtils.isEmpty(city)) {
                    CustomToast.show(BuyActivity.this, "Please enter the City", R.drawable.error);
                } else if (TextUtils.isEmpty(postal)) {
                    CustomToast.show(BuyActivity.this, "Please enter the Postal Code", R.drawable.error);
                } else {

                    firestore.collection("cart")
                            .whereEqualTo("mobile", Usermobile)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                    Cart cartItem = document.toObject(Cart.class);
                                    purchasedItems.add(cartItem);
                                }
                                Log.d("PayHere_Debug", "Cart Items Loaded: " + purchasedItems);
                            })
                            .addOnFailureListener(e -> Log.e("PayHere_Debug", "Error loading cart items", e));

                    initiatePayment(formattedTotal);
                }
            }
        });
    }

    private void initiatePayment(String amountFormatted) {
        try {
            InitRequest req = new InitRequest();

            // Merchant and transaction details
            req.setMerchantId(MERCHANT_ID);
            req.setCurrency("LKR");
            req.setAmount(Double.parseDouble(amountFormatted));
            req.setOrderId(generateUniqueOrderId());
            req.setItemsDescription("Order Payment");

            // Customer details
            req.getCustomer().setFirstName(fnameEditText.getText().toString());
            req.getCustomer().setLastName(lnameEditText.getText().toString());
            req.getCustomer().setEmail(userEmail != null ? userEmail : "customer@example.com");
            req.getCustomer().setPhone(mobileEditText.getText().toString());

            // Address details
            req.getCustomer().getAddress().setAddress(line1EditText.getText().toString());
            req.getCustomer().getAddress().setCity(cityEditText.getText().toString());
            req.getCustomer().getAddress().setCountry("Sri Lanka");

            // Optional delivery address
            req.getCustomer().getDeliveryAddress().setAddress(line2EditText.getText().toString());
            req.getCustomer().getDeliveryAddress().setCity(cityEditText.getText().toString());
            req.getCustomer().getDeliveryAddress().setCountry("Sri Lanka");

            // Add item details
            req.getItems().add(new Item(null, "Order Payment", 1, Double.parseDouble(amountFormatted)));

            // Set sandbox environment
            PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);

            // Start PayHere activity
            Intent intent = new Intent(this, PHMainActivity.class);
            intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
            payHereLauncher.launch(intent);

        } catch (Exception e) {
            Log.e("PayHere_Debug", "Exception in initiatePayment", e);
            e.printStackTrace();
        }
    }

    private String generateUniqueOrderId() {
        return "ORDER_" + System.currentTimeMillis();
    }
}