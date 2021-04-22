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

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class SettingsFragment extends PreferenceFragmentCompat {
    private MainActivity mActivity;
    private Repository mRepo;
    private int mExtra;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mRepo = new Repository(mActivity);
        mExtra = 0;

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
        prefSupport.setEnabled(false);
        prefSupport.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_site))));
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
}