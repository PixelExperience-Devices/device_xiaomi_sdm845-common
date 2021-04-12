/*
 * Copyright (C) 2020 The LineageOS Project
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

package org.lineageos.settings.thermal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.IOException;

public final class ThermalUtils {

    private static final String TAG = "ThermalUtils";
    private static final String THERMAL_CONTROL = "thermal_control";

    protected static final int STATE_DEFAULT = 0;
    protected static final int STATE_GAMING = 1;

    private static final String THERMAL_PROP_DEFAULT = "-novr";
    private static final String THERMAL_PROP_GAMING = "-gaming";

    private static final String THERMAL_GAMING = "thermal.gaming=";

    private static final String VENDOR_THERMAL_PROP = "vendor.thermal.vr_mode";

    private SharedPreferences mSharedPrefs;

    protected ThermalUtils(Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void startService(Context context) {
        context.startServiceAsUser(new Intent(context, ThermalService.class),
                UserHandle.CURRENT);
    }

    private void writeValue(String profiles) {
        mSharedPrefs.edit().putString(THERMAL_CONTROL, profiles).apply();
    }

    private String getValue() {
        String value = mSharedPrefs.getString(THERMAL_CONTROL, null);

        if (value == null || value.isEmpty()) {
            value = THERMAL_GAMING;
            writeValue(value);
        }
        return value;
    }

    protected void writePackage(String packageName, int mode) {
        String value = getValue();
        value = value.replace(packageName + ",", "");
        String[] modes = value.split(":");
        String finalString;

        switch (mode) {
            case STATE_GAMING:
                modes[0] = modes[0] + packageName + ",";
                break;
        }

        finalString = modes[0];

        writeValue(finalString);
    }

    protected int getStateForPackage(String packageName) {
        String value = getValue();
        String[] modes = value.split(":");
        int state = STATE_DEFAULT;
        if (modes[0].contains(packageName + ",")) {
            state = STATE_GAMING;
        }

        return state;
    }

    protected void setThermalProfile(String packageName) {
        String value = getValue();
        String modes[];
        String prop = THERMAL_PROP_DEFAULT;

        if (value != null) {
            modes = value.split(":");

            if (modes[0].contains(packageName + ",")) {
                prop = THERMAL_PROP_GAMING;
            }
        }

        try {
            SystemProperties.set(VENDOR_THERMAL_PROP, prop);
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to set " + VENDOR_THERMAL_PROP + "to " + prop, e);
        }
    }
}
