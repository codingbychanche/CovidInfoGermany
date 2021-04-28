package com.berthold.covidinfo;

import android.os.Bundle;

import com.berthold.covidinfo.ui.home.FragmentFavCovidDataView;
import com.berthold.covidinfo.ui.home.FragmentFavCovidDataViewModel;
import com.berthold.covidinfo.ui.home.FragmentLocalData;
import com.berthold.covidinfo.ui.home.FragmentSearchViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    private FragmentSearchViewModel fragmentSearchViewModel;
    private FragmentFavCovidDataViewModel fragmentFavCovidDataViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // Start local covid data view und favourite data view fragment.
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();

        // Show local covid Data
        ft.setReorderingAllowed(true);
        ft.replace(R.id.fragment_local_covid_data_view, new FragmentLocalData(), "Fragment_1");
        ft.commitAllowingStateLoss();

        // Show favourite location
        FragmentTransaction ft2=manager.beginTransaction();
        ft2.setReorderingAllowed(true);
        ft2.replace(R.id.fragment_fav_covid_data_view, new FragmentFavCovidDataView(), "Fragment_1");
        ft2.commitAllowingStateLoss();

        // View model providers
        fragmentSearchViewModel =new ViewModelProvider(this).get(FragmentSearchViewModel.class);
        fragmentFavCovidDataViewModel=new ViewModelProvider(this).get(FragmentFavCovidDataViewModel.class);

        // Restore each view model's shared preferences...
        fragmentSearchViewModel.restoreFromSharedPreferences(this);
        fragmentFavCovidDataViewModel.restoreFromSharedPreferences(this);

    }

    /**
     * From here all view models dedicated methods are invoked
     * when the life cycle state changes to the destroyed state.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        fragmentSearchViewModel.storeToSHaredPreffs(this);
        fragmentFavCovidDataViewModel.storeToSHaredPreffs(this);
    }

    @Override
    public void onBackPressed(){
        fragmentSearchViewModel.storeToSHaredPreffs(this);
        fragmentFavCovidDataViewModel.storeToSHaredPreffs(this);
        finish();
    }
}