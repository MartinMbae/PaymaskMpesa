package com.example.paymaskmpesa;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import mehdi.sakout.fancybuttons.FancyButton;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity{


    LinearLayout layoutBottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    ProgressDialog progressDialog;

    private TextInputEditText phone, amount;
    private TextView dialogAmountText, dialogPhoneNumberText;

    private int amountToPay;
    private String finalPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutBottomSheet = findViewById(R.id.bottom_sheetWhereFrom);
        FancyButton btn_proceed_payment = findViewById(R.id.btn_proceed_payment);
        phone = findViewById(R.id.phoneNumber);
        amount = findViewById(R.id.amount);
        dialogAmountText = findViewById(R.id.dialog_amount);
        dialogPhoneNumberText = findViewById(R.id.dialog_phone);

        bottomSheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        btn_proceed_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payAction(amountToPay, finalPhoneNumber);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
    }

    private void payAction(int amount, String phoneNumber) {
        JsonObject jsonObject = new JsonObject();

        int businessShortCode = 174379;

        jsonObject.addProperty("ConsumerKey", "7v40tkzdWyzK7WvSBOa9IThIEPu31d40");
        jsonObject.addProperty("ConsumerSecret", "7dVQ3nIIi7bwPEbp");
        jsonObject.addProperty("BusinessShortCode", businessShortCode);
        jsonObject.addProperty("PassKey", "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919");
        jsonObject.addProperty("TransactionType", "CustomerPayBillOnline");
        jsonObject.addProperty("Amount", amount);
        jsonObject.addProperty("PartyA", phoneNumber);
        jsonObject.addProperty("PartyB", businessShortCode);
        jsonObject.addProperty("PhoneNumber", phoneNumber);
        jsonObject.addProperty("CallBackURL", "kasmyap305eba3df274cbcad1bd9b27ca4f3160c7ef39c9b09a89747513cf4ecb7e06bdf53de5e635e2fb55a31949f4627195ebf49d56339e938ff8418a06f28d216d47Hgnroz5BUphNOSe67wlSBzQuPlW8WrVnn6+8t7c/XYprlZMRuZ8beuuc1fttXquh4VlpcyOjrrGUzlAy5zx0dK5Du/f+B/z8CotE4fY1mi01Ol6I392MDzwBCxzaQkOpkasmyap");
        jsonObject.addProperty("AccountReference", "Services and goods");
        jsonObject.addProperty("TransactionDesc", "Payment for goods via M-PESA.");
//        jsonObject.addProperty("AppendData", "/12/kenya"); //An optional Field to pass any parameters through the callback.


        submitData(jsonObject);
    }

    private void submitData(JsonObject jsonObject) {

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Processing Payment");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<JsonObject> call = service.postData(jsonObject);
        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    String response_string = response.body().toString();
                    try {
                        JSONObject obj = new JSONObject(response_string);
                        String success = obj.getString("success");
                        String message = obj.getString("message");
                        if (success.equalsIgnoreCase("true")) {
                            showSuccessfulDialog("Success", message);
                        } else {
                            showSuccessfulDialog("Failed", message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showSuccessfulDialog("Failed", "Something went wrong. Please try again " + e.getMessage());

                    }
                } else {
                    showSuccessfulDialog("Failed", "Something went wrong. Please try again ");

                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressDialog.dismiss();
                showSuccessfulDialog("Failed", "You do not have good internet connection. Please check your internet connection or try again later");
            }
        });
    }

    void showSuccessfulDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    public void pay(View view) {


        String phoneN = phone.getText().toString();
        String am   = amount.getText().toString();

        if (TextUtils.isEmpty(phoneN) || !TextUtils.isDigitsOnly(phoneN.trim()) ||  !phoneN.startsWith("254") ||  phoneN.trim().length() != 12 ){
            phone.setError("Provide valid phone number");
            return;
        }


        if (TextUtils.isEmpty(am.trim()) || !TextUtils.isDigitsOnly(am.trim())){
            amount.setError("Provide valid amount");
            return;
        }

        amountToPay = Integer.parseInt(am.trim());
        finalPhoneNumber = phoneN.trim();

        String amountFormatted = "Ksh. "+ amountToPay;
        dialogPhoneNumberText.setText(finalPhoneNumber);
        dialogAmountText.setText(amountFormatted);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private interface ApiService {
        @POST("api/pay")
        Call<JsonObject> postData(@Body JsonObject body);
    }

    @Override
    public void onBackPressed() {

        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }else {
            super.onBackPressed();
        }
    }
}