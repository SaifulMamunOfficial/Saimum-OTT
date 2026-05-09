package nemosofts.tamilaudiopro.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;

import java.util.List;

import com.saimum.saimummusic.BuildConfig;
import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;
import nemosofts.tamilaudiopro.utils.purchases.BillingConnector;
import nemosofts.tamilaudiopro.utils.purchases.BillingEventListener;
import nemosofts.tamilaudiopro.utils.purchases.enums.ProductType;
import nemosofts.tamilaudiopro.utils.purchases.models.BillingResponse;
import nemosofts.tamilaudiopro.utils.purchases.models.ProductInfo;
import nemosofts.tamilaudiopro.utils.purchases.models.PurchaseInfo;

public class BillingConnectorActivity extends AppCompatActivity {

    private String subscriptionID;
    private String baseKey;

    private String planName;
    private String planPrice;
    private String planDuration;
    private String planCurrencyCode;
    private TextView btnProceed;
    private SPHelper spHelper;

    private BillingConnector billingConnector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bg_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> finish());

        spHelper = new SPHelper(this);

        Intent intent = getIntent();
        planName = intent.getStringExtra("planName");
        planPrice = intent.getStringExtra("planPrice");
        planDuration = intent.getStringExtra("planDuration");
        planCurrencyCode= intent.getStringExtra("planCurrencyCode");

        subscriptionID = intent.getStringExtra("subscription_id");
        baseKey = intent.getStringExtra("base_key");

        initViews();
        initializeBillingClient();

        btnProceed.setOnClickListener(v -> billingConnector.subscribe(BillingConnectorActivity.this, subscriptionID));

        findViewById(R.id.tv_terms).setOnClickListener(v -> openWebActivity());
        findViewById(R.id.changePlan).setOnClickListener(view -> finish());
    }

    private void initializeBillingClient() {
        billingConnector = new BillingConnector(this, baseKey)
                .setSubscriptionId(subscriptionID)
                .autoAcknowledge()
                .connect();

        billingConnector.setBillingEventListener(new BillingEventListener() {
            @Override
            public void onProductsFetched(@NonNull List<ProductInfo> productDetails) {
                // this method is empty
            }

            //this IS the listener in which we can restore previous purchases
            @Override
            public void onPurchasedProductsFetched(@NonNull ProductType productType, @NonNull List<PurchaseInfo> purchases) {
                if (isFinishing()){
                    return;
                }

                int isAcknowledged = 0;
                if(!purchases.isEmpty()) {
                    boolean isSubscribed = spHelper.getIsSubscribed();
                    for (int i = 0; i < purchases.size(); i++) {
                        if (!isSubscribed && (purchases.get(i).getProduct().equalsIgnoreCase(subscriptionID) && (purchases.get(i).isAcknowledged()))) {
                            isAcknowledged++;
                        }
                    }
                }

                if (isAcknowledged > 0){
                    btnProceed.setVisibility(View.INVISIBLE);
                    spHelper.setIsSubscribed(true);
                    Toast.makeText(BillingConnectorActivity.this, "The previous purchase was successfully restored.", Toast.LENGTH_SHORT).show();
                    openMainActivity();
                }
            }

            //this IS NOT the listener in which we'll give user entitlement for purchases (see ReadMe.md why)
            @Override
            public void onProductsPurchased(@NonNull List<PurchaseInfo> purchases) {
                // this method is empty
            }

            //this IS the listener in which we'll give user entitlement for purchases (the ReadMe.md explains why)
            @Override
            public void onPurchaseAcknowledged(@NonNull PurchaseInfo purchase) {
                /*
                 * Grant user entitlement for NON-CONSUMABLE products and SUBSCRIPTIONS here
                 *
                 * Even though onProductsPurchased is triggered when a purchase is successfully made
                 * there might be a problem along the way with the payment and the purchase won't be acknowledged
                 *
                 * Google will refund users purchases that aren't acknowledged in 3 days
                 *
                 * To ensure that all valid purchases are acknowledged the library will automatically
                 * check and acknowledge all unacknowledged products at the startup
                 * */
                if (isFinishing()){
                    return;
                }
                String acknowledgedProduct = purchase.getProduct();
                if (acknowledgedProduct.equalsIgnoreCase(subscriptionID)) {
                    btnProceed.setVisibility(View.INVISIBLE);
                    spHelper.setIsSubscribed(true);
                    Toast.makeText(BillingConnectorActivity.this, "The purchase was successfully made.", Toast.LENGTH_SHORT).show();
                    openMainActivity();
                }
            }

            @Override
            public void onPurchaseConsumed(@NonNull PurchaseInfo purchase) {
                /*
                 * Grant user entitlement for CONSUMABLE products here
                 *
                 * Even though onProductsPurchased is triggered when a purchase is successfully made
                 * there might be a problem along the way with the payment and the user will be able consume the product
                 * without actually paying
                 * */
            }

            @Override
            public void onBillingError(@NonNull BillingConnector billingConnector, @NonNull BillingResponse response) {
                if (isFinishing()){
                    return;
                }
                String errorMsg;
                switch (response.getErrorType()) {
                    case CLIENT_NOT_READY -> errorMsg = "Client is not ready yet";
                    case CLIENT_DISCONNECTED -> errorMsg = "Client has disconnected";
                    case PRODUCT_NOT_EXIST -> errorMsg = "Product does not exist";
                    case CONSUME_ERROR -> errorMsg = "Error during consumption";
                    case CONSUME_WARNING -> errorMsg = "Warning during consumption";
                    case ACKNOWLEDGE_ERROR -> errorMsg = "Error during acknowledgment";
                    case ACKNOWLEDGE_WARNING ->
                            errorMsg = "The transaction is still pending. Please come back later to receive the purchase!";
                    case FETCH_PURCHASED_PRODUCTS_ERROR ->
                            errorMsg = "Error occurred while querying purchased products";
                    case BILLING_ERROR ->
                            errorMsg = "Error occurred during initialization / querying product details";
                    case USER_CANCELED -> errorMsg = "User pressed back or canceled a dialog";
                    case SERVICE_UNAVAILABLE -> errorMsg = "Check your internet connection!";
                    case BILLING_UNAVAILABLE -> errorMsg = "Billing is unavailable at the moment.";
                    case ITEM_UNAVAILABLE ->
                            errorMsg = "Requested product is not available for purchase";
                    case DEVELOPER_ERROR -> errorMsg = "Invalid arguments provided to the API";
                    case ERROR -> errorMsg = "Something happened, the transaction was canceled!";
                    case ITEM_ALREADY_OWNED ->
                            errorMsg = "Failure to purchase since item is already owned";
                    case ITEM_NOT_OWNED -> errorMsg = "Failure to consume since item is not owned";
                    default -> errorMsg = "Unknown error occurred";
                }
                Toast.makeText(BillingConnectorActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openMainActivity() {
        Callback.setRecreate(true);
        Intent intent = new Intent(BillingConnectorActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void openWebActivity() {
        Intent intent = new Intent(BillingConnectorActivity.this, WebActivity.class);
        intent.putExtra("web_url", BuildConfig.BASE_URL+"data.php?terms");
        intent.putExtra("page_title", getResources().getString(R.string.terms_and_conditions));
        startActivity(intent);
    }

    @SuppressLint("SetTextI18n")
    private void initViews() {
        TextView textPlanName = findViewById(R.id.textPackName);
        TextView textPlanPrice = findViewById(R.id.textPrice);
        TextView textPlanCurrency = findViewById(R.id.textCurrency);
        TextView textPlanDuration = findViewById(R.id.textDay);
        TextView textChoosePlanName = findViewById(R.id.choosePlanName);
        TextView textEmail = findViewById(R.id.planEmail);

        btnProceed = findViewById(R.id.tv_btn_proceed);
        btnProceed.setText("Pay for "+ planPrice+" "+planCurrencyCode);

        textPlanName.setText(planName);
        textPlanPrice.setText(planPrice);
        textPlanDuration.setText(getString(R.string.plan_day_for, planDuration)+" Days");
        textChoosePlanName.setText(planName);
        textEmail.setText(spHelper.getEmail());
        textPlanCurrency.setText(planCurrencyCode);
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_billing_connector;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingConnector != null) {
            billingConnector.release();
        }
    }
}