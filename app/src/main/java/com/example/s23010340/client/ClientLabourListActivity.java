package com.example.s23010340.client;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.s23010340.R;
import com.example.s23010340.authentication.labour.LabourDatabaseHelper;
import com.example.s23010340.authentication.labour.LabourLocationMapActivity;
import com.example.s23010340.authentication.labour.LabourProfile;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ClientLabourListActivity extends AppCompatActivity {
    private LabourDatabaseHelper labourDatabaseHelper;
    private LinearLayout labourContainer;
    private EditText searchInput;
    private ActivityResultLauncher<Intent> paymentLauncher;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_labour_list);

        category = getIntent().getStringExtra(ClientDashboardActivity.EXTRA_CATEGORY);
        if (category == null || category.trim().isEmpty()) {
            category = getString(R.string.electrician);
        }

        labourDatabaseHelper = new LabourDatabaseHelper(this);
        labourContainer = findViewById(R.id.labour_list_container);
        searchInput = findViewById(R.id.search_input);
        paymentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }
                int paidLabourId = result.getData()
                    .getIntExtra(ClientPaymentActivity.EXTRA_PAYMENT_SUCCESS_LABOUR_ID, -1);
                if (paidLabourId > 0) {
                    labourDatabaseHelper.markLabourAsHired(paidLabourId);
                    Toast.makeText(this, R.string.hiring_confirmed, Toast.LENGTH_SHORT).show();
                    loadLabours(searchInput.getText().toString().trim());
                }
            }
        );

        TextView titleView = findViewById(R.id.labour_list_title);
        titleView.setText(category);

        ImageView serviceImage = findViewById(R.id.service_image);
        setImageFromAssets(serviceImage, getCategoryAsset(category));

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadLabours(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        findViewById(R.id.back_button).setOnClickListener(view -> finish());

        loadLabours("");
    }

    private void loadLabours(String searchText) {
        labourContainer.removeAllViews();
        List<LabourProfile> labourProfiles = labourDatabaseHelper.getLaboursByCategory(category, searchText);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (LabourProfile labourProfile : labourProfiles) {
            View card = inflater.inflate(R.layout.item_labour_card, labourContainer, false);

            TextView nameView = card.findViewById(R.id.labour_name_value);
            TextView categoryView = card.findViewById(R.id.labour_category_value);
            TextView phoneView = card.findViewById(R.id.labour_phone_value);
            TextView districtView = card.findViewById(R.id.labour_district_value);
            TextView cityView = card.findViewById(R.id.labour_city_value);
            ImageView profileImageView = card.findViewById(R.id.labour_photo);
            TextView statusView = card.findViewById(R.id.labour_status_text);
            View statusDot = card.findViewById(R.id.labour_status_dot);
            Button hiringButton = card.findViewById(R.id.make_hiring_button);
            View mapButton = card.findViewById(R.id.map_action_button);
            View whatsappButton = card.findViewById(R.id.whatsapp_action_button);
            View actionContainer = card.findViewById(R.id.hired_actions_container);

            nameView.setText(labourProfile.getName());
            categoryView.setText(labourProfile.getCategory());
            phoneView.setText(labourProfile.isHired() ? toInternationalPhone(labourProfile.getPhone()) : maskPhone(labourProfile.getPhone()));
            districtView.setText(labourProfile.getDistrict());
            cityView.setText(labourProfile.getCity());
            setProfileImage(profileImageView, labourProfile.getPhotoUri());
            mapButton.setOnClickListener(view -> openMap(
                labourProfile.getLocation(),
                labourProfile.getDistrict(),
                labourProfile.getCity()
            ));

            if (labourProfile.isHired()) {
                statusDot.setBackgroundResource(R.drawable.bg_status_available);
                statusView.setText(R.string.available);
                hiringButton.setEnabled(false);
                hiringButton.setBackgroundResource(R.drawable.bg_button_hired);
                hiringButton.setText(R.string.hired);
                actionContainer.setVisibility(View.VISIBLE);
                mapButton.setVisibility(View.VISIBLE);
                whatsappButton.setVisibility(View.VISIBLE);
                whatsappButton.setOnClickListener(view -> openWhatsApp(labourProfile.getPhone()));
            } else if (labourProfile.isAvailable()) {
                statusDot.setBackgroundResource(R.drawable.bg_status_available);
                statusView.setText(R.string.available);
                hiringButton.setEnabled(true);
                hiringButton.setBackgroundResource(R.drawable.bg_button_hire);
                hiringButton.setText(R.string.make_hiring);
                actionContainer.setVisibility(View.VISIBLE);
                mapButton.setVisibility(View.VISIBLE);
                whatsappButton.setVisibility(View.GONE);
            } else {
                statusDot.setBackgroundResource(R.drawable.bg_status_not_available);
                statusView.setText(R.string.not_available);
                hiringButton.setEnabled(false);
                hiringButton.setBackgroundResource(R.drawable.bg_button_hire_disabled);
                hiringButton.setText(R.string.not_available);
                actionContainer.setVisibility(View.VISIBLE);
                mapButton.setVisibility(View.VISIBLE);
                whatsappButton.setVisibility(View.GONE);
            }

            hiringButton.setOnClickListener(view -> {
                if (!labourProfile.isAvailable() || labourProfile.isHired()) {
                    return;
                }
                Intent intent = new Intent(this, ClientPaymentActivity.class);
                intent.putExtra(ClientPaymentActivity.EXTRA_LABOUR_ID, labourProfile.getId());
                intent.putExtra(ClientPaymentActivity.EXTRA_LABOUR_NAME, labourProfile.getName());
                intent.putExtra(ClientPaymentActivity.EXTRA_CATEGORY, labourProfile.getCategory());
                paymentLauncher.launch(intent);
            });

            labourContainer.addView(card);
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "**********";
        }
        String lastFour = phone.substring(phone.length() - 4);
        return "******" + lastFour;
    }

    private String toInternationalPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "";
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.startsWith("0") && digits.length() >= 10) {
            return "+94" + digits.substring(1);
        }
        if (digits.startsWith("94")) {
            return "+" + digits;
        }
        return phone;
    }

    private void setProfileImage(ImageView imageView, String photoUri) {
        imageView.setImageResource(R.drawable.welcome_logo);
        if (photoUri == null || photoUri.trim().isEmpty()) {
            return;
        }
        try {
            imageView.setImageURI(Uri.parse(photoUri));
            if (imageView.getDrawable() == null) {
                imageView.setImageResource(R.drawable.welcome_logo);
            }
        } catch (SecurityException | IllegalArgumentException exception) {
            imageView.setImageResource(R.drawable.welcome_logo);
        }
    }

    private void openMap(String location, String district, String city) {
        Intent intent = new Intent(this, LabourLocationMapActivity.class);
        intent.putExtra(LabourLocationMapActivity.EXTRA_LOCATION, location);
        intent.putExtra(LabourLocationMapActivity.EXTRA_DISTRICT, district);
        intent.putExtra(LabourLocationMapActivity.EXTRA_CITY, city);
        startActivity(intent);
    }

    private void openWhatsApp(String phone) {
        String internationalPhone = toInternationalPhone(phone).replace("+", "");
        String message = Uri.encode(getString(R.string.whatsapp_message));
        Uri uri = Uri.parse("https://wa.me/" + internationalPhone + "?text=" + message);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.whatsapp_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    private String getCategoryAsset(String categoryName) {
        if (getString(R.string.electrician).equalsIgnoreCase(categoryName)) {
            return "Electrician.png";
        }
        if (getString(R.string.plumber).equalsIgnoreCase(categoryName)) {
            return "plumber-with-plumbing-wrench-logo-technical-service-logo-emblem-construction-repair-work-vector-illustration-2P55REH-removebg-preview.png";
        }
        if (getString(R.string.carpenter).equalsIgnoreCase(categoryName)) {
            return "Carpenter.png";
        }
        if (getString(R.string.welder).equalsIgnoreCase(categoryName)) {
            return "Welder.png";
        }
        if (getString(R.string.mason).equalsIgnoreCase(categoryName)) {
            return "Mason.png";
        }
        if (getString(R.string.mechanic).equalsIgnoreCase(categoryName)) {
            return "mechanic-repair-.png";
        }
        if (getString(R.string.painter).equalsIgnoreCase(categoryName)) {
            return "painter-logo-vector-art-illustration-7_666870-2862-removebg-preview.png";
        }
        if (getString(R.string.roofer).equalsIgnoreCase(categoryName)) {
            return "roofer.png";
        }
        if (getString(R.string.gardening).equalsIgnoreCase(categoryName)) {
            return "Gardening.png";
        }
        return "Electrician.png";
    }

    private void setImageFromAssets(ImageView imageView, String assetName) {
        AssetManager assetManager = getAssets();
        try (InputStream inputStream = assetManager.open(assetName)) {
            Drawable drawable = Drawable.createFromStream(inputStream, null);
            imageView.setImageDrawable(drawable);
        } catch (IOException e) {
            imageView.setImageDrawable(null);
        }
    }
}
