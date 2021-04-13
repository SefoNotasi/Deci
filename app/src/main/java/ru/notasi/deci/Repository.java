package ru.notasi.deci;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import java.util.Locale;

public class Repository {
    private final SharedPreferences mPrefs;
    private final Context mContext;

    public Repository(Context context) {
        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public int getLevel() {
        return mPrefs.getInt(Constants.KEY_LEVEL, 0);
    }

    public void setLevel(int level) {
        mPrefs.edit().putInt(Constants.KEY_LEVEL, level).apply();
    }

    public int getDifficulty() {
        return mPrefs.getInt(Constants.KEY_DIFFICULTY, 1);
    }

    public void setDifficulty(int difficulty) {
        mPrefs.edit().putInt(Constants.KEY_DIFFICULTY, difficulty).apply();
    }

    public int getExperience() {
        return mPrefs.getInt(Constants.KEY_EXPERIENCE, 0);
    }

    public void setExperience(int experience) {
        mPrefs.edit().putInt(Constants.KEY_EXPERIENCE, experience).apply();
    }

    public int getExpRequired() {
        return mPrefs.getInt(Constants.KEY_EXP_REQUIRED, 5);
    }

    public void setExpRequired(int expRequired) {
        mPrefs.edit().putInt(Constants.KEY_EXP_REQUIRED, expRequired).apply();
    }

    public int getSkin() {
        return mPrefs.getInt(Constants.KEY_SKIN, 0);
    }

    public void setSkin(int skin) {
        mPrefs.edit().putInt(Constants.KEY_SKIN, skin).apply();
    }

    public int getIdImageCoin() {
        return mPrefs.getInt(Constants.KEY_ID_IMAGE_COIN, R.drawable.image_coin_heads);
    }

    public void setIdImageCoin(int id) {
        mPrefs.edit().putInt(Constants.KEY_ID_IMAGE_COIN, id).apply();
    }

    public int getCountFlipsForRate() {
        return mPrefs.getInt(Constants.KEY_COUNT_FLIPS_FOR_RATE, 0);
    }

    public void setCountFlipsForRate(int count) {
        mPrefs.edit().putInt(Constants.KEY_COUNT_FLIPS_FOR_RATE, count).apply();
    }

    public int getCountFlips() {
        return mPrefs.getInt(Constants.KEY_COUNT_FLIPS, 0);
    }

    public void setCountFlips(int countFlips) {
        mPrefs.edit().putInt(Constants.KEY_COUNT_FLIPS, countFlips).apply();
    }

    public int getCountHeads() {
        return mPrefs.getInt(Constants.KEY_COUNT_HEADS, 0);
    }

    public void setCountHeads(int countHeads) {
        mPrefs.edit().putInt(Constants.KEY_COUNT_HEADS, countHeads).apply();
    }

    public int getCountTails() {
        return mPrefs.getInt(Constants.KEY_COUNT_TAILS, 0);
    }

    public void setCountTails(int countTails) {
        mPrefs.edit().putInt(Constants.KEY_COUNT_TAILS, countTails).apply();
    }

    public int getCountEdges() {
        return mPrefs.getInt(Constants.KEY_COUNT_EDGES, 0);
    }

    public void setCountEdges(int countEdges) {
        mPrefs.edit().putInt(Constants.KEY_COUNT_EDGES, countEdges).apply();
    }

    public String getLastFlipShare() {
        return mPrefs.getString(Constants.KEY_LAST_FLIP_SHARE, Constants.TEXT_ZERO);
    }

    public void setLastFlipShare(String lastFlipShare) {
        mPrefs.edit().putString(Constants.KEY_LAST_FLIP_SHARE, lastFlipShare).apply();
    }

    public String getLastFlipStats() {
        return mPrefs.getString(Constants.KEY_LAST_FLIP_STATS, Constants.TEXT_ZERO);
    }

    public void setLastFlipStats(String lastFlipStats) {
        mPrefs.edit().putString(Constants.KEY_LAST_FLIP_STATS, lastFlipStats).apply();
    }

    public String getLang() {
        return mPrefs.getString(Constants.KEY_SETTING_LANG, Constants.LANG_SYSTEM);
    }

    public void setLang(String lang) {
        mPrefs.edit().putString(Constants.KEY_SETTING_LANG, lang).apply();
    }

    public void useLang(String lang) {
        Locale loc = null;
        switch (lang) {
            case Constants.LANG_SYSTEM:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    loc = Resources
                            .getSystem()
                            .getConfiguration()
                            .getLocales()
                            .get(0);
                } else {
                    loc = Resources
                            .getSystem()
                            .getConfiguration()
                            .locale;
                }
                break;
            case Constants.LANG_EN:
                loc = new Locale(Constants.LANG_EN);
                break;
            case Constants.LANG_RU:
                loc = new Locale(Constants.LANG_RU);
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (LocaleList
                    .getDefault()
                    .get(0)
                    .getLanguage()
                    .equals(loc.getLanguage())) return;
        } else {
            if (Locale
                    .getDefault()
                    .getLanguage()
                    .equals(loc.getLanguage())) return;
        } // else {
        Locale.setDefault(loc);
        Resources resources = mContext.getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics metrics = resources.getDisplayMetrics();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(loc);
        } else {
            config.locale = loc;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mContext.createConfigurationContext(config);
        } else {
            resources.updateConfiguration(config, metrics);
        }
        refresh();
    }

    public String getTheme() {
        return mPrefs.getString(Constants.KEY_SETTING_THEME, Constants.THEME_AUTO);
    }

    public void setTheme(String theme) {
        mPrefs.edit().putString(Constants.KEY_SETTING_THEME, theme).apply();
    }

    public void useTheme(String theme) {
        switch (theme) {
            case Constants.THEME_AUTO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case Constants.THEME_DAY:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case Constants.THEME_NIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }

    public boolean getTips() {
        return mPrefs.getBoolean(Constants.KEY_SETTING_TIPS, true);
    }

    public void setTips(boolean tips) {
        mPrefs.edit().putBoolean(Constants.KEY_SETTING_TIPS, tips).apply();
    }

    public boolean getSound() {
        return mPrefs.getBoolean(Constants.KEY_SETTING_SOUND, true);
    }

    public void setSound(boolean sound) {
        mPrefs.edit().putBoolean(Constants.KEY_SETTING_SOUND, sound).apply();
    }

    public float getVolume() {
        return mPrefs.getFloat(Constants.KEY_SETTING_VOLUME, 0.5f);
    }

    public void setVolume(float volume) {
        mPrefs.edit().putFloat(Constants.KEY_SETTING_VOLUME, volume).apply();
    }

    public boolean getVibro() {
        return mPrefs.getBoolean(Constants.KEY_SETTING_VIBRO, true);
    }

    public void setVibro(boolean vibro) {
        mPrefs.edit().putBoolean(Constants.KEY_SETTING_VIBRO, vibro).apply();
    }

    public int getVibroForce() {
        return mPrefs.getInt(Constants.KEY_SETTING_VIBRO_FORCE, 100);
    }

    public void setVibroForce(int force) {
        mPrefs.edit().putInt(Constants.KEY_SETTING_VIBRO_FORCE, force).apply();
    }

    public boolean getShake() {
        return mPrefs.getBoolean(Constants.KEY_SETTING_SHAKE, true);
    }

    public void setShake(boolean shake) {
        mPrefs.edit().putBoolean(Constants.KEY_SETTING_SHAKE, shake).apply();
    }

    public int getShakeSense() {
        return mPrefs.getInt(Constants.KEY_SETTING_SHAKE_SENSE, 5);
    }

    public void setShakeSense(int sense) {
        mPrefs.edit().putInt(Constants.KEY_SETTING_SHAKE_SENSE, sense).apply();
    }

    public void resetStats() {
        setLevel(0);
        setDifficulty(1);
        setExperience(0);
        setExpRequired(5);
        setSkin(0);
        setLastFlipShare(Constants.TEXT_ZERO);
        setLastFlipStats(Constants.TEXT_ZERO);
        setCountFlipsForRate(0);
        setCountFlips(0);
        setCountHeads(0);
        setCountTails(0);
        setCountEdges(0);
    }

    public void refresh() {
        ((MainActivity) mContext).recreate();
    }

    public void restoreSettings() {
        setLang(Constants.LANG_SYSTEM);
        setTheme(Constants.THEME_AUTO);
        setTips(true);
        setSound(true);
        setVolume(0.5f);
        setVibro(true);
        setVibroForce(100);
        setShake(true);
        setShakeSense(5);
        refresh();
    }
}