package mdad.localdata.androide_library;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtils {
    public static void applyTheme(int themeMode) {
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }
}

