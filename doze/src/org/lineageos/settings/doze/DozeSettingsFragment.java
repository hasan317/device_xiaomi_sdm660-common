/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2019,2021 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.doze;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.CompoundButton;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.android.settingslib.widget.MainSwitchPreference;

public class DozeSettingsFragment extends PreferenceFragment
        implements CompoundButton.OnCheckedChangeListener, OnPreferenceChangeListener {

    private MainSwitchPreference mSwitchBar;

    private SwitchPreference mWakeOnGesturePreference;
    private SwitchPreference mPickUpPreference;
    private SwitchPreference mHandwavePreference;
    private SwitchPreference mPocketPreference;

    private Handler mHandler = new Handler();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.doze_settings);

        SharedPreferences prefs = getActivity().getSharedPreferences("doze_settings",
                Activity.MODE_PRIVATE);
        if (savedInstanceState == null && !prefs.getBoolean("first_help_shown", false)) {
            showHelp();
        }

        boolean dozeEnabled = Utils.isDozeEnabled(getActivity());

        mSwitchBar = (MainSwitchPreference) findPreference(Utils.DOZE_ENABLE);
        mSwitchBar.addOnSwitchChangeListener(this);
        mSwitchBar.setChecked(dozeEnabled);

        mWakeOnGesturePreference = (SwitchPreference) findPreference(Utils.WAKE_ON_GESTURE_KEY);
        mWakeOnGesturePreference.setEnabled(dozeEnabled);
        mWakeOnGesturePreference.setOnPreferenceChangeListener(this);

        PreferenceCategory proximitySensorCategory =
                (PreferenceCategory) getPreferenceScreen().findPreference(Utils.CATEG_PROX_SENSOR);

        mPickUpPreference = (SwitchPreference) findPreference(Utils.GESTURE_PICK_UP_KEY);
        mPickUpPreference.setEnabled(dozeEnabled);
        mPickUpPreference.setOnPreferenceChangeListener(this);

        mHandwavePreference = (SwitchPreference) findPreference(Utils.GESTURE_HAND_WAVE_KEY);
        mHandwavePreference.setEnabled(dozeEnabled);
        mHandwavePreference.setOnPreferenceChangeListener(this);

        mPocketPreference = (SwitchPreference) findPreference(Utils.GESTURE_POCKET_KEY);
        mPocketPreference.setEnabled(dozeEnabled);
        mPocketPreference.setOnPreferenceChangeListener(this);

        // Hide proximity sensor related features if the device doesn't support them
        if (!Utils.getProxCheckBeforePulse(getActivity())) {
            getPreferenceScreen().removePreference(proximitySensorCategory);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        mHandler.post(() -> Utils.checkDozeService(getActivity()));
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Utils.enableDoze(getActivity(), isChecked);
        Utils.checkDozeService(getActivity());

        mSwitchBar.setChecked(isChecked);

        mWakeOnGesturePreference.setEnabled(isChecked);
        mPickUpPreference.setEnabled(isChecked);
        mHandwavePreference.setEnabled(isChecked);
        mPocketPreference.setEnabled(isChecked);
    }

    private void showHelp() {
        AlertDialog helpDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.doze_settings_help_title)
                .setMessage(R.string.doze_settings_help_text)
                .setPositiveButton(R.string.dialog_ok,
                        (dialog, which) -> {
                            getActivity()
                                    .getSharedPreferences("doze_settings", Activity.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("first_help_shown", true)
                                    .commit();
                            dialog.cancel();
                        })
                .create();
        helpDialog.show();
    }
}
