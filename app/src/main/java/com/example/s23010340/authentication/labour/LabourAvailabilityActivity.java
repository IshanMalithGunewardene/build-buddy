package com.example.s23010340.authentication.labour;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.s23010340.MainActivity;
import com.example.s23010340.R;
import com.example.s23010340.authentication.SessionManager;

public class LabourAvailabilityActivity extends AppCompatActivity {
    public static final String EXTRA_LABOUR_EMAIL = "extra_labour_email";

    private LabourDatabaseHelper databaseHelper;
    private String labourEmail;

    private Spinner statusSpinner;
    private Spinner categorySpinner;
    private EditText nameInput;
    private EditText phoneInput;
    private EditText locationInput;
    private EditText districtInput;
    private EditText cityInput;
    private EditText mapSearchInput;
    private ImageView profilePhotoPreview;
    private Button editButton;
    private Button saveButton;
    private Button cancelButton;
    private Button pickPhotoButton;
    private Button mapSearchButton;
    private Button useMapLocationButton;
    private WebView mapPreviewWebView;
    private ActivityResultLauncher<String[]> imagePickerLauncher;
    private String selectedPhotoUri;
    private String currentMapQuery;
    private boolean hasExistingProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.labour_availability);

        labourEmail = getIntent().getStringExtra(EXTRA_LABOUR_EMAIL);
        if (labourEmail == null || labourEmail.trim().isEmpty()) {
            finish();
            return;
        }

        databaseHelper = new LabourDatabaseHelper(this);

        statusSpinner = findViewById(R.id.labour_status_spinner);
        categorySpinner = findViewById(R.id.labour_category_spinner);
        nameInput = findViewById(R.id.labour_name_input);
        phoneInput = findViewById(R.id.labour_phone_input);
        locationInput = findViewById(R.id.labour_location_input);
        districtInput = findViewById(R.id.labour_district_input);
        cityInput = findViewById(R.id.labour_city_input);
        mapSearchInput = findViewById(R.id.labour_map_search_input);
        mapSearchButton = findViewById(R.id.labour_map_search_button);
        useMapLocationButton = findViewById(R.id.labour_use_map_location_button);
        mapPreviewWebView = findViewById(R.id.labour_map_preview_webview);
        profilePhotoPreview = findViewById(R.id.labour_profile_photo_preview);
        editButton = findViewById(R.id.labour_edit_button);
        saveButton = findViewById(R.id.labour_save_button);
        cancelButton = findViewById(R.id.labour_cancel_button);
        pickPhotoButton = findViewById(R.id.labour_pick_photo_button);

        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri == null) {
                    return;
                }
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException securityException) {
                    Toast.makeText(this, R.string.profile_photo_permission_failed, Toast.LENGTH_SHORT).show();
                }
                selectedPhotoUri = uri.toString();
                showProfilePhoto(selectedPhotoUri);
            }
        );

        setupSpinners();
        setupMapPreview();
        loadExistingProfile();

        findViewById(R.id.location_map_button).setOnClickListener(view -> {
            String district = districtInput.getText().toString().trim();
            String city = cityInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();
            String query = (location + " " + city + " " + district).trim();
            if (query.isEmpty()) {
                query = district.isEmpty() ? city : district + " " + city;
            }
            mapSearchInput.setText(query);
            searchLocationOnMap(query);
        });

        mapSearchButton.setOnClickListener(view -> {
            String query = mapSearchInput.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(this, R.string.enter_map_location, Toast.LENGTH_SHORT).show();
                return;
            }
            searchLocationOnMap(query);
        });
        useMapLocationButton.setOnClickListener(view -> {
            if (currentMapQuery == null || currentMapQuery.trim().isEmpty()) {
                Toast.makeText(this, R.string.enter_map_location, Toast.LENGTH_SHORT).show();
                return;
            }
            locationInput.setText(currentMapQuery);
            Toast.makeText(this, R.string.location_set_from_map, Toast.LENGTH_SHORT).show();
        });
        editButton.setOnClickListener(view -> setEditingMode(true));
        saveButton.setOnClickListener(view -> saveProfile());
        cancelButton.setOnClickListener(view -> loadExistingProfile());
        pickPhotoButton.setOnClickListener(view -> imagePickerLauncher.launch(new String[] {"image/*"}));
        findViewById(R.id.labour_logout_button).setOnClickListener(view -> {
            new SessionManager(this).clearSession();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.labour_status_values,
            android.R.layout.simple_spinner_item
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.labour_category_values,
            android.R.layout.simple_spinner_item
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
    }

    private void loadExistingProfile() {
        LabourProfile profile = databaseHelper.getLabourProfileByEmail(labourEmail);
        if (profile == null) {
            hasExistingProfile = false;
            selectedPhotoUri = null;
            statusSpinner.setSelection(0);
            categorySpinner.setSelection(0);
            nameInput.setText("");
            phoneInput.setText("");
            locationInput.setText("");
            districtInput.setText("");
            cityInput.setText("");
            showProfilePhoto(null);
            setEditingMode(true);
            return;
        }

        hasExistingProfile = true;
        selectedPhotoUri = profile.getPhotoUri();
        statusSpinner.setSelection(profile.isAvailable() ? 0 : 1);
        setSpinnerValue(categorySpinner, profile.getCategory());
        nameInput.setText(profile.getName());
        phoneInput.setText(profile.getPhone());
        locationInput.setText(profile.getLocation());
        districtInput.setText(profile.getDistrict());
        cityInput.setText(profile.getCity());
        showProfilePhoto(profile.getPhotoUri());
        mapSearchInput.setText(profile.getLocation());
        searchLocationOnMap(profile.getLocation());
        setEditingMode(false);
    }

    private void saveProfile() {
        String status = String.valueOf(statusSpinner.getSelectedItem());
        String category = String.valueOf(categorySpinner.getSelectedItem());
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String district = districtInput.getText().toString().trim();
        String city = cityInput.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || location.isEmpty() || district.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean available = getString(R.string.available).equalsIgnoreCase(status);
        boolean success = databaseHelper.upsertLabourAvailabilityProfile(
            labourEmail, name, category, phone, location, district, city, selectedPhotoUri, available
        );

        if (success) {
            hasExistingProfile = true;
            setEditingMode(false);
            Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.profile_save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void setEditingMode(boolean editingEnabled) {
        statusSpinner.setEnabled(editingEnabled);
        categorySpinner.setEnabled(editingEnabled);
        nameInput.setEnabled(editingEnabled);
        phoneInput.setEnabled(editingEnabled);
        locationInput.setEnabled(editingEnabled);
        districtInput.setEnabled(editingEnabled);
        cityInput.setEnabled(editingEnabled);
        mapSearchInput.setEnabled(editingEnabled);
        mapSearchButton.setEnabled(editingEnabled);
        useMapLocationButton.setEnabled(editingEnabled);
        pickPhotoButton.setEnabled(editingEnabled);
        pickPhotoButton.setAlpha(editingEnabled ? 1f : 0.5f);
        mapSearchButton.setAlpha(editingEnabled ? 1f : 0.5f);
        useMapLocationButton.setAlpha(editingEnabled ? 1f : 0.5f);

        if (hasExistingProfile) {
            editButton.setVisibility(editingEnabled ? View.GONE : View.VISIBLE);
            saveButton.setVisibility(editingEnabled ? View.VISIBLE : View.GONE);
            cancelButton.setVisibility(editingEnabled ? View.VISIBLE : View.GONE);
        } else {
            editButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        }
    }

    private void showProfilePhoto(String uriString) {
        if (uriString == null || uriString.trim().isEmpty()) {
            profilePhotoPreview.setImageResource(R.drawable.welcome_logo);
            return;
        }
        try {
            profilePhotoPreview.setImageURI(Uri.parse(uriString));
            if (profilePhotoPreview.getDrawable() == null) {
                profilePhotoPreview.setImageResource(R.drawable.welcome_logo);
            }
        } catch (SecurityException | IllegalArgumentException exception) {
            profilePhotoPreview.setImageResource(R.drawable.welcome_logo);
            Toast.makeText(this, R.string.profile_photo_load_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null) {
            return;
        }
        for (int i = 0; i < spinner.getCount(); i++) {
            String item = String.valueOf(spinner.getItemAtPosition(i));
            if (item.equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void setupMapPreview() {
        WebSettings settings = mapPreviewWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        mapPreviewWebView.setWebViewClient(new WebViewClient());
        searchLocationOnMap("Sri Lanka");
    }

    private void searchLocationOnMap(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        currentMapQuery = query.trim();
        String encodedQuery = Uri.encode(currentMapQuery);
        String mapsApiKey = getMapsApiKey();
        if (mapsApiKey.isEmpty()) {
            mapPreviewWebView.loadUrl("https://www.google.com/maps/search/?api=1&query=" + encodedQuery);
            return;
        }
        String embedUrl = "https://www.google.com/maps/embed/v1/place?key="
            + Uri.encode(mapsApiKey)
            + "&q="
            + encodedQuery;
        String html = "<!DOCTYPE html><html><body style='margin:0;padding:0;'>"
            + "<iframe width='100%' height='100%' style='border:0;' loading='lazy' allowfullscreen "
            + "referrerpolicy='no-referrer-when-downgrade' src='"
            + embedUrl
            + "'></iframe></body></html>";
        mapPreviewWebView.loadDataWithBaseURL("https://www.google.com", html, "text/html", "UTF-8", null);
    }

    private String getMapsApiKey() {
        try {
            ApplicationInfo appInfo;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appInfo = getPackageManager().getApplicationInfo(
                    getPackageName(),
                    PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA)
                );
            } else {
                appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            }
            if (appInfo.metaData == null) {
                return "";
            }
            String key = appInfo.metaData.getString("com.google.android.geo.API_KEY", "");
            return key == null ? "" : key.trim();
        } catch (PackageManager.NameNotFoundException exception) {
            return "";
        }
    }
}
