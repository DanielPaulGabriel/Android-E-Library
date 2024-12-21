package mdad.localdata.androide_library;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StaffActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int savedThemeMode = SharedPrefsManager.getThemeMode(this);
        AppCompatDelegate.setDefaultNightMode(savedThemeMode);
        setContentView(R.layout.activity_staff);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(new StaffBooksFragment());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_catalog) {
                selectedFragment = new StaffBooksFragment();
            } else if (item.getItemId() == R.id.nav_reviews) {
                selectedFragment = new StaffUserActivityFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new StaffAccountsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}