package ru.notasi.deci;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat {
    private MainActivity mActivity;
    private Repository mRepo;
    private int mExtra;
    private BillingClient mBillingClient;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mRepo = new Repository(mActivity);
        mExtra = 0;
        initBilling();

        ((FloatingActionButton) mActivity.findViewById(R.id.button_share)).hide();
        ((ExtendedFloatingActionButton) mActivity.findViewById(R.id.button_auto)).hide();
        FloatingActionButton actionButton = mActivity.findViewById(R.id.button_action);
        actionButton.setImageResource(R.drawable.ic_baseline_thumb_up_24);
        actionButton.show();
        actionButton.setOnClickListener(view12 -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_store)))));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        Preference prefLang = findPreference(Constants.KEY_SETTING_LANG);
        prefLang.setEnabled(false);
        prefLang.setOnPreferenceChangeListener((preference, newValue) -> {
            mRepo.useLang(newValue.toString());
            return true;
        });

        Preference prefTheme = findPreference(Constants.KEY_SETTING_THEME);
        prefTheme.setOnPreferenceChangeListener((preference, newValue) -> {
            mRepo.useTheme(newValue.toString());
            return true;
        });

        Preference prefVibroForce = findPreference(Constants.KEY_SETTING_VIBRO_FORCE);
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prefVibroForce.setEnabled(vibrator.hasAmplitudeControl());
        } else prefVibroForce.setEnabled(false);

        Preference prefSupport = findPreference(Constants.KEY_SETTING_SUPPORT);
        prefSupport.setOnPreferenceClickListener(preference -> {
            if (mBillingClient.isReady()) {
                queryProducts();
            } else {
                initBilling();
                mActivity.showToast(mActivity.getString(R.string.toast_support_error));
            }
            return true;
        });

        Preference prefRefresh = findPreference(Constants.KEY_SETTING_REFRESH);
        prefRefresh.setOnPreferenceClickListener(preference -> {
            mRepo.refresh();
            if (mRepo.getTips())
                mActivity.showToast(getString(R.string.toast_refresh));
            return true;
        });

        Preference prefRestore = findPreference(Constants.KEY_SETTING_RESTORE);
        prefRestore.setOnPreferenceClickListener(preference -> {
            Snackbar.make(getView(),
                    getString(R.string.snack_settings),
                    Snackbar.LENGTH_LONG)
                    .setAnchorView(mActivity.findViewById(R.id.button_action))
                    .setAction(getString(R.string.snack_yes), view11 -> {
                        mRepo.restoreSettings();
                        mActivity.showToast(getString(R.string.toast_restore));
                    }).show();
            return true;
        });

        Preference prefVersion = findPreference(Constants.KEY_SETTING_VERSION);
        prefVersion.setSummary(((MainActivity) getActivity()).getVersion()); // TODO: Fix mActivity NPE.
        prefVersion.setOnPreferenceClickListener(preference -> {
            mExtra++;
            if (mExtra < Constants.COUNT_SKINS) {
                if (mRepo.getTips())
                    mActivity.showToast(getString(R.string.toast_more));
            } else {
//                int cheat = mRepo.getLevel() + mExtra; TODO: Make code.
//                mRepo.setLevel(cheat);
                if (mRepo.getTips())
                    mActivity.showToast(getString(R.string.toast_extra));
            }
            return true;
        });
    }

    // Initialize a BillingClient.
    private void initBilling() {
        PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                        && purchases != null) {
                    for (Purchase purchase : purchases) {
                        handlePurchase(purchase);
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                    mActivity.showToast(mActivity.getString(R.string.toast_support_cancel));
                } else {
                    // Handle any other error codes.
                    mActivity.showToast(mActivity.getString(R.string.toast_support_error));
                }
            }
        };

        mBillingClient = BillingClient.newBuilder(mActivity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        // Establish a connection to Google Play.
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    // Show products available to buy.
    private void queryProducts() {
        List<String> skuList = new ArrayList<>();
        skuList.add(Constants.PRODUCT_ID);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        mBillingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        // Process the result.

                        // Launch the purchase flow.
                        // An activity reference from which the billing flow will be launched.

                        // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetailsList.get(0))
                                .build();

                        int responseCode = mBillingClient.launchBillingFlow(mActivity, billingFlowParams).getResponseCode();

                        // Handle the result.
                    }
                });
    }

    // Processing purchases.
    private void handlePurchase(Purchase purchase) {
        // Purchase retrieved from BillingClient#queryPurchases or your PurchasesUpdatedListener.

        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.

        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Handle the success of the consume operation.
                    mActivity.showToast(mActivity.getString(R.string.toast_support_ok));
                }
            }
        };

        mBillingClient.consumeAsync(consumeParams, listener);
    }
}