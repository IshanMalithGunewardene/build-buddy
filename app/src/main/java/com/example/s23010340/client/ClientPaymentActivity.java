package com.example.s23010340.client;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.s23010340.R;

public class ClientPaymentActivity extends AppCompatActivity {
    public static final String EXTRA_LABOUR_ID = "extra_labour_id";
    public static final String EXTRA_LABOUR_NAME = "extra_labour_name";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_PAYMENT_SUCCESS_LABOUR_ID = "extra_payment_success_labour_id";

    private static final int HIRING_FEE = 500;
    private static final int PLATFORM_FEE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_payment);

        TextView paymentTitle = findViewById(R.id.payment_title);
        TextView hiringFeeValue = findViewById(R.id.hiring_fee_value);
        TextView platformFeeValue = findViewById(R.id.platform_fee_value);
        TextView totalAmountValue = findViewById(R.id.total_amount_value);
        TextView payButton = findViewById(R.id.pay_button);

        String labourName = getIntent().getStringExtra(EXTRA_LABOUR_NAME);
        String category = getIntent().getStringExtra(EXTRA_CATEGORY);
        int labourId = getIntent().getIntExtra(EXTRA_LABOUR_ID, -1);
        if (labourName == null) {
            labourName = "";
        }
        if (category == null) {
            category = "";
        }

        String titleText = labourName.isEmpty() ? getString(R.string.payment) : labourName + " - " + category;
        paymentTitle.setText(titleText);

        int totalAmount = HIRING_FEE + PLATFORM_FEE;
        hiringFeeValue.setText(getString(R.string.money_format, HIRING_FEE));
        platformFeeValue.setText(getString(R.string.money_format, PLATFORM_FEE));
        totalAmountValue.setText(getString(R.string.money_format, totalAmount));
        payButton.setText(getString(R.string.pay_money_format, totalAmount));

        EditText cardNumberInput = findViewById(R.id.card_number_input);
        EditText expiryInput = findViewById(R.id.expiry_input);
        EditText cvvInput = findViewById(R.id.cvv_input);
        EditText cardHolderInput = findViewById(R.id.cardholder_input);

        findViewById(R.id.back_button).setOnClickListener(view -> finish());
        findViewById(R.id.cancel_button).setOnClickListener(view -> finish());

        findViewById(R.id.pay_button).setOnClickListener(view -> {
            String cardNumber = cardNumberInput.getText().toString().trim();
            String expiry = expiryInput.getText().toString().trim();
            String cvv = cvvInput.getText().toString().trim();
            String cardHolder = cardHolderInput.getText().toString().trim();

            if (cardNumber.isEmpty() || expiry.isEmpty() || cvv.isEmpty() || cardHolder.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_payment_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            if (cardNumber.length() < 12 || cvv.length() < 3) {
                Toast.makeText(this, R.string.invalid_payment_details, Toast.LENGTH_SHORT).show();
                return;
            }

            completePayment(labourId);
        });

        findViewById(R.id.demo_payment_button).setOnClickListener(view -> completePayment(labourId));
    }

    private void completePayment(int labourId) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_PAYMENT_SUCCESS_LABOUR_ID, labourId);
        setResult(RESULT_OK, resultIntent);
        Toast.makeText(this, R.string.payment_success, Toast.LENGTH_SHORT).show();
        finish();
    }
}
