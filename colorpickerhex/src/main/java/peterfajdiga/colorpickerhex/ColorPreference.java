/*
 Originally by CyanogenMod
 Modified by Peter Fajdiga

 Original license follows:
 * Copyright (C) 2012 The CyanogenMod Project
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

package peterfajdiga.colorpickerhex;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ColorPreference extends DialogPreference {

    private static String TAG = "AppLightPreference";
    public static final int DEFAULT_TIME = 1000;
    public static final int DEFAULT_COLOR = 0xffffff;

    private ImageView mLightColorView;

    private int mColorValue;

    private Resources mResources;

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mColorValue = DEFAULT_COLOR;
        init();
    }

    public ColorPreference(Context context, int color) {
        super(context, null);
        mColorValue = color;
        init();
    }

    private void init() {
        setLayoutResource(R.layout.preference_application_light);
        mResources = getContext().getResources();
    }

//    public void onStart() {
//        LightSettingsDialog d = (LightSettingsDialog) getDialog();
//        if (d != null) {
//            d.onStart();
//        }
//    }
//
//    public void onStop() {
//        LightSettingsDialog d = (LightSettingsDialog) getDialog();
//        if (d != null) {
//            d.onStop();
//        }
//    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mLightColorView = (ImageView)view.findViewById(R.id.light_color);

        // Hide the summary text - it takes up too much space on a low res device
        // We use it for storing the package name for the longClickListener
        TextView tView = (TextView)view.findViewById(android.R.id.summary);
        tView.setVisibility(View.GONE);

        updatePreferenceViews();
    }

    private void updatePreferenceViews() {
        final int size = (int) mResources.getDimension(R.dimen.oval_notification_size);

        if (mLightColorView != null) {
            mLightColorView.setEnabled(true);
            // adjust if necessary to prevent material whiteout
            final int imageColor = ((mColorValue & 0xF0F0F0) == 0xF0F0F0) ?
                    (mColorValue - 0x101010) : mColorValue;
            mLightColorView.setImageDrawable(createOvalShape(size,
                    0xFF000000 + imageColor));
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        final LightSettingsDialog d = (LightSettingsDialog) getDialog();
    }

//    @Override
    protected Dialog createDialog() {
        final LightSettingsDialog d = new LightSettingsDialog(getContext(), mColorValue);
        d.setAlphaSliderVisible(true);

        d.setButton(AlertDialog.BUTTON_POSITIVE, mResources.getString(R.string.dlg_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setColor(d.getColor());
                        callChangeListener(this);
                    }
                });
        d.setButton(AlertDialog.BUTTON_NEGATIVE, mResources.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        return d;
    }

    /**
     * Getters and Setters
     */

    public int getColor() {
        return mColorValue;
    }

    public void setColor(int color) {
        mColorValue = color;
        updatePreferenceViews();
        persistInt(color);
    }

    /**
     * Utility methods
     */
    private static ShapeDrawable createOvalShape(int size, int color) {
        ShapeDrawable shape = new ShapeDrawable(new OvalShape());
        shape.setIntrinsicHeight(size);
        shape.setIntrinsicWidth(size);
        shape.getPaint().setColor(color);
        return shape;
    }
}
