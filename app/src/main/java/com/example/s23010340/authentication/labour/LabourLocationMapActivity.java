package com.example.s23010340.authentication.labour;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import com.example.s23010340.R;

public class LabourLocationMapActivity extends AppCompatActivity {
    public static final String EXTRA_LOCATION = "extra_location";
    public static final String EXTRA_DISTRICT = "extra_district";
    public static final String EXTRA_CITY = "extra_city";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.labour_location_map);

        String location = getIntent().getStringExtra(EXTRA_LOCATION);
        String district = getIntent().getStringExtra(EXTRA_DISTRICT);
        String city = getIntent().getStringExtra(EXTRA_CITY);

        if (location == null) {
            location = "";
        }
        if (district == null) {
            district = "";
        }
        if (city == null) {
            city = "";
        }

        String query = location;
        if (!city.isEmpty()) {
            query = query + " " + city;
        }
        if (!district.isEmpty()) {
            query = query + " " + district;
        }
        if (query.trim().isEmpty()) {
            query = "Kandy Sri Lanka";
        }

        String encodedQuery = Uri.encode(query);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + encodedQuery));
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
            finish();
            return;
        }

        WebView mapWebView = findViewById(R.id.map_web_view);
        WebSettings settings = mapWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        mapWebView.setWebViewClient(new WebViewClient());
        mapWebView.loadUrl("https://www.google.com/maps/search/?api=1&query=" + encodedQuery);

        findViewById(R.id.map_back_button).setOnClickListener(view -> finish());
    }
}
