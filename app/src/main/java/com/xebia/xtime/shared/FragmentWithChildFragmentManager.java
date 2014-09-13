package com.xebia.xtime.shared;

import android.app.Fragment;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Workaround for an exception when instantiating Fragments that use the child fragment manager,
 * e.g. fragments that contain a ViewPager.
 *
 * @see <a href="https://code.google.com/p/android/issues/detail?id=42601#c10">Android project
 * issue 42601</a>
 */
public class FragmentWithChildFragmentManager extends Fragment {

    private static final String TAG = "FragmentWithChildFragmentManager";
    private static final Field sChildFragmentManagerField;

    static {
        Field f = null;
        try {
            f = Fragment.class.getDeclaredField("mChildFragmentManager");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Error getting mChildFragmentManager field", e);
        }
        sChildFragmentManagerField = f;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (sChildFragmentManagerField != null) {
            try {
                sChildFragmentManagerField.set(this, null);
            } catch (Exception e) {
                Log.e(TAG, "Error setting mChildFragmentManager field", e);
            }
        }
    }
}
