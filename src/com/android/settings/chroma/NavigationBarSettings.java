package com.android.settings.chroma;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.cm.ScreenType;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

public class NavigationBarSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String KILL_APP_LONGPRESS_BACK = "kill_app_longpress_back";
    private static final String PREF_NAVIGATION_BAR_HEIGHT = "navigation_bar_height";
    private static final String PREF_NAVIGATION_BAR_HEIGHT_LANDSCAPE = "navigation_bar_height_landscape";
    private static final String PREF_NAVIGATION_BAR_WIDTH = "navigation_bar_width";
    private static final String SHOW_CLEAR_ALL_RECENTS = "show_clear_all_recents";
    private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";

    ListPreference mNavigationBarHeight;
    ListPreference mNavigationBarHeightLandscape;
    ListPreference mNavigationBarWidth;

    private ListPreference mRecentsClearAllLocation;
    private SwitchPreference mKillAppLongPressBack;
    private SwitchPreference mRecentsClearAll;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.chroma_settings_navigation);

        PreferenceScreen prefSet = getPreferenceScreen();
    ContentResolver resolver = getActivity().getContentResolver();

        // kill-app long press back
        mKillAppLongPressBack = (SwitchPreference) findPreference(KILL_APP_LONGPRESS_BACK);
        mKillAppLongPressBack.setOnPreferenceChangeListener(this);
        int killAppLongPressBack = Settings.Secure.getInt(getContentResolver(),
                KILL_APP_LONGPRESS_BACK, 0);
        mKillAppLongPressBack.setChecked(killAppLongPressBack != 0);

        // clear all recents
        mRecentsClearAll = (SwitchPreference) prefSet.findPreference(SHOW_CLEAR_ALL_RECENTS);

        mRecentsClearAllLocation = (ListPreference) prefSet.findPreference(RECENTS_CLEAR_ALL_LOCATION);
        int location = Settings.System.getIntForUser(resolver,
                Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
        mRecentsClearAllLocation.setValue(String.valueOf(location));
        mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
        mRecentsClearAllLocation.setOnPreferenceChangeListener(this);

        // navigation bar dimensions
        mNavigationBarHeight =
            (ListPreference) findPreference(PREF_NAVIGATION_BAR_HEIGHT);
        mNavigationBarHeight.setOnPreferenceChangeListener(this);
/* tablets
        mNavigationBarHeightLandscape =
            (ListPreference) findPreference(PREF_NAVIGATION_BAR_HEIGHT_LANDSCAPE);

        if (ScreenType.isPhone(getActivity())) {
            prefSet.removePreference(mNavigationBarHeightLandscape);
            mNavigationBarHeightLandscape = null;
        } else {
            mNavigationBarHeightLandscape.setOnPreferenceChangeListener(this);
        }
*/
        mNavigationBarWidth =
            (ListPreference) findPreference(PREF_NAVIGATION_BAR_WIDTH);

        if (!ScreenType.isPhone(getActivity())) {
            prefSet.removePreference(mNavigationBarWidth);
            mNavigationBarWidth = null;
        } else {
            mNavigationBarWidth.setOnPreferenceChangeListener(this);
        }

        updateDimensionValues();

    }

    private void updateDimensionValues() {
        int navigationBarHeight = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_HEIGHT, -1);
        if (navigationBarHeight == -1) {
            navigationBarHeight = (int) (getResources().getDimension(
                    com.android.internal.R.dimen.navigation_bar_height)
                    / getResources().getDisplayMetrics().density);
        }
        mNavigationBarHeight.setValue(String.valueOf(navigationBarHeight));
        mNavigationBarHeight.setSummary(mNavigationBarHeight.getEntry());

        if (mNavigationBarHeightLandscape != null) {
            int navigationBarHeightLandscape = Settings.System.getInt(getContentResolver(),
                                Settings.System.NAVIGATION_BAR_HEIGHT_LANDSCAPE, -1);
            if (navigationBarHeightLandscape == -1) {
                navigationBarHeightLandscape = (int) (getResources().getDimension(
                        com.android.internal.R.dimen.navigation_bar_height_landscape)
                        / getResources().getDisplayMetrics().density);
            }
            mNavigationBarHeightLandscape.setValue(String.valueOf(navigationBarHeightLandscape));
            mNavigationBarHeightLandscape.setSummary(mNavigationBarHeightLandscape.getEntry());
        }

        if (mNavigationBarWidth != null) {
            int navigationBarWidth = Settings.System.getInt(getContentResolver(),
                                Settings.System.NAVIGATION_BAR_WIDTH, -1);
            if (navigationBarWidth == -1) {
                navigationBarWidth = (int) (getResources().getDimension(
                        com.android.internal.R.dimen.navigation_bar_width)
                        / getResources().getDisplayMetrics().density);
            }
            mNavigationBarWidth.setValue(String.valueOf(navigationBarWidth));
            mNavigationBarWidth.setSummary(mNavigationBarWidth.getEntry());
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mKillAppLongPressBack) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(), KILL_APP_LONGPRESS_BACK,
                    value ? 1 : 0);
            return true;
        } else if (preference == mNavigationBarWidth) {
            int index = mNavigationBarWidth.findIndexOfValue((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_WIDTH, Integer.parseInt((String) objValue));
            updateDimensionValues();
            return true;
        } else if (preference == mNavigationBarHeight) {
            int index = mNavigationBarHeight.findIndexOfValue((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_HEIGHT, Integer.parseInt((String) objValue));
            updateDimensionValues();
            return true;
        } else if (preference == mNavigationBarHeightLandscape) {
            int index = mNavigationBarHeightLandscape.findIndexOfValue((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_HEIGHT_LANDSCAPE, Integer.parseInt((String) objValue));
            updateDimensionValues();
            return true;
        } else if (preference == mRecentsClearAllLocation) {
            int location = Integer.valueOf((String) objValue);
            int index = mRecentsClearAllLocation.findIndexOfValue((String) objValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, location, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
            return true;
        }
            return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.NAVBAR_SETTINGS;
    }
}
