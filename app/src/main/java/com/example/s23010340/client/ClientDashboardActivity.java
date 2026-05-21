package com.example.s23010340.client;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.s23010340.MainActivity;
import com.example.s23010340.R;
import com.example.s23010340.authentication.SessionManager;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import java.io.IOException;
import java.io.InputStream;

public class ClientDashboardActivity extends AppCompatActivity {
    public static final String EXTRA_CATEGORY = "extra_category";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_dashboard);

        setImageFromAssets(R.id.build_buddy_logo, "build_buddy.svg");
        setImageFromAssets(R.id.logout_button, "logout button.png");
        setImageFromAssets(R.id.electrician_icon, "Electrician.png");
        setImageFromAssets(R.id.plumber_icon, "plumber-with-plumbing-wrench-logo-technical-service-logo-emblem-construction-repair-work-vector-illustration-2P55REH-removebg-preview.png");
        setImageFromAssets(R.id.carpenter_icon, "Carpenter.png");
        setImageFromAssets(R.id.welder_icon, "Welder.png");
        setImageFromAssets(R.id.mason_icon, "Mason.png");
        setImageFromAssets(R.id.mechanic_icon, "mechanic-repair-.png");
        setImageFromAssets(R.id.painter_icon, "painter-logo-vector-art-illustration-7_666870-2862-removebg-preview.png");
        setImageFromAssets(R.id.roofer_icon, "roofer.png");
        setImageFromAssets(R.id.gardening_icon, "Gardening.png");

        bindServiceClick(R.id.electrician_icon, getString(R.string.electrician));
        bindServiceClick(R.id.plumber_icon, getString(R.string.plumber));
        bindServiceClick(R.id.carpenter_icon, getString(R.string.carpenter));
        bindServiceClick(R.id.welder_icon, getString(R.string.welder));
        bindServiceClick(R.id.mason_icon, getString(R.string.mason));
        bindServiceClick(R.id.mechanic_icon, getString(R.string.mechanic));
        bindServiceClick(R.id.painter_icon, getString(R.string.painter));
        bindServiceClick(R.id.roofer_icon, getString(R.string.roofer));
        bindServiceClick(R.id.gardening_icon, getString(R.string.gardening));

        findViewById(R.id.logout_button).setOnClickListener(view -> {
            new SessionManager(this).clearSession();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void bindServiceClick(int viewId, String category) {
        findViewById(viewId).setOnClickListener(view -> {
            Intent intent = new Intent(this, ClientLabourListActivity.class);
            intent.putExtra(EXTRA_CATEGORY, category);
            startActivity(intent);
        });
    }

    private void setImageFromAssets(int viewId, String assetName) {
        ImageView imageView = findViewById(viewId);
        AssetManager assetManager = getAssets();
        try (InputStream inputStream = assetManager.open(assetName)) {
            if (assetName.toLowerCase().endsWith(".svg")) {
                SVG svg = SVG.getFromInputStream(inputStream);
                imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                imageView.setImageDrawable(new PictureDrawable(svg.renderToPicture()));
            } else {
                Drawable drawable = Drawable.createFromStream(inputStream, null);
                imageView.setLayerType(View.LAYER_TYPE_NONE, null);
                imageView.setImageDrawable(drawable);
            }
        } catch (IOException | SVGParseException e) {
            imageView.setImageDrawable(null);
        }
    }
}
