package org.ieselcaminas.pmdm.inappbilling2018;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener, ConsumeResponseListener {

    public static final String ITEM_TO_BUY = "android.test.purchased"; // "buyclick";
    private BillingClient mBillingClient;
    private String purchaseToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBillingClient = BillingClient.newBuilder(this).setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                    Toast.makeText(MainActivity.this, "Connected to Google Play", Toast.LENGTH_LONG).show();


                    List<String> skuList = new ArrayList<>();
                    skuList.add(ITEM_TO_BUY);
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                    mBillingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                                    Toast.makeText(MainActivity.this, "received response", Toast.LENGTH_LONG).show();
                                    Log.d("victorDetail", ""+responseCode);
                                    for (SkuDetails detail : skuDetailsList) {
                                        Log.d("victorDetail", detail.toString());
                                        if (detail.getSku().equals(ITEM_TO_BUY)) {
                                            TextView textViewPrice = findViewById(R.id.textViewPrice);
                                            textViewPrice.setText(detail.getPrice());
                                        }
                                    }
                                }
                    });

                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Toast.makeText(MainActivity.this, "Not connected Google Play", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void buyClick(View view) {
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSku(ITEM_TO_BUY)
                .setType(BillingClient.SkuType.INAPP) // SkuType.SUB for subscription
                .build();
        int responseCode = mBillingClient.launchBillingFlow(this, flowParams);
        Log.d("victorDetail", "Response code: " + responseCode);
    }

    public void buttonClicked(View view) {
        purchaseToken = "inapp:" + getPackageName() + ":" + ITEM_TO_BUY;
        mBillingClient.consumeAsync(purchaseToken, this);
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        Log.d("victorDetail", ""+purchases);
        if (responseCode == BillingClient.BillingResponse.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    public void handlePurchase(Purchase purchase) {
        if (purchase.getSku().equals(ITEM_TO_BUY)) {
            Button buttonClick = findViewById(R.id.clickButton);
            Button buttonBuy = findViewById(R.id.buyButton);
            buttonBuy.setEnabled(false);
            buttonClick.setEnabled(true);
            purchaseToken = purchase.getPurchaseToken();
            Log.d("victorDetail", "purchaseToken: " + purchaseToken);
        }
    }

    @Override
    public void onConsumeResponse(int responseCode, String purchaseToken) {
        Button buttonClick = findViewById(R.id.clickButton);
        Button buttonBuy = findViewById(R.id.buyButton);
        buttonBuy.setEnabled(true);
        buttonClick.setEnabled(false);
    }

}
