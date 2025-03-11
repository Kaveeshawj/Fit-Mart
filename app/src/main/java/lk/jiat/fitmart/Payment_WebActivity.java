package lk.jiat.fitmart;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Payment_WebActivity extends AppCompatActivity {
    private static final int PAYMENT_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_web);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        WebView webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new CustomWebViewClient());

        String paymentUrl = getIntent().getStringExtra("PAYMENT_URL");
        if (paymentUrl != null) {
            webView.loadUrl(paymentUrl);
        } else {
            Toast.makeText(this, "Invalid Payment URL", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // Payment completion checks
            if (url.contains("success")) {
                setResult(RESULT_OK);
                finish();
            } else if (url.contains("cancel")) {
                setResult(RESULT_CANCELED);
                finish();
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
