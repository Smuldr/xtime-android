package com.xebia.xtime.shared;

import android.support.v4.app.Fragment;

import java.lang.reflect.Field;

import timber.log.Timber;

/**
 * Workaround for an exception when instantiating Fragments that use the child fragment manager,
 * e.g. fragments that contain a ViewPager.
 *
 * @see <a href="https://code.google.com/p/android/issues/detail?id=42601#c10">Android project
 * issue 42601</a>
 */
public class FragmentWithChildFragmentManager extends Fragment {

    private static final Field sChildFragmentManagerField;

    static {
        Field f = null;
        try {
            f = Fragment.class.getDeclaredField("mChildFragmentManager");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Timber.e(e, "Error getting mChildFragmentManager field");
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
                Timber.e(e, "Error setting mChildFragmentManager field");
            }
        }
    }
}
