package com.berthold.covidinfo;

import android.os.Bundle;
import android.util.Log;

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

import java.io.File;
import java.sql.Connection;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    // View Models
    private FragmentSearchViewModel fragmentSearchViewModel;
    private FragmentFavCovidDataViewModel fragmentFavCovidDataViewModel;

    // Database
    public static Connection covidDataBase;

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
        FragmentTransaction ft2 = manager.beginTransaction();
        ft2.setReorderingAllowed(true);
        ft2.replace(R.id.fragment_fav_covid_data_view, new FragmentFavCovidDataView(), "Fragment_2");
        ft2.commitAllowingStateLoss();

        // View model providers
        fragmentSearchViewModel = new ViewModelProvider(this).get(FragmentSearchViewModel.class);
        fragmentFavCovidDataViewModel = new ViewModelProvider(this).get(FragmentFavCovidDataViewModel.class);

        // toDo: How do we handle shared prefs....?
        // Restore each view model's shared preferences...
        fragmentSearchViewModel.restoreFromSharedPreferences(this);
        fragmentFavCovidDataViewModel.restoreFromSharedPreferences(this);

        //
        //
        // Set up the database holding covid data for the local and favourite locaions...
        //
        //
        String dbName = "/covidData";

        // Database will be created inside the app's private directory.
        // CAUTION: This way the database will be deleted, when the app is uninstalled!
        File f = getFilesDir();
        String path = (f.getAbsolutePath() + dbName);

        try {
            // Creates a new database if no database could be found.
            CovidDataBase.make(path);
            Log.i("DBMAKE", "DB Created on:\n");
            Log.i("DBMAKE", path);
        } catch (Exception e) {
            Log.i("DBMAKE", "Error creating DB:" + e.toString());
        }

        // Read DB
        String DB_DRIVER = "org.h2.Driver";
        String DB_CONNECTION = "jdbc:h2:" + path;
        String DB_USER = "";
        String DB_PASSWORD = "";

        try {
            Log.d("DBMAKE", "Reading:" + DB_CONNECTION + "\n");
            covidDataBase = CovidDataBase.getDataBase(DB_DRIVER, DB_CONNECTION, DB_USER, DB_PASSWORD);

        } catch (Exception e) {
            Log.d("DBMAKE", "Error opening DB\n");
            Log.d("DBMAKE", e.toString());
        }

        // Test database functions....
        StringBuffer r = CovidDataBase.sqlRequest("Select * from covid", covidDataBase);
        Log.v("DBMAKE", r.toString());
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
    public void onBackPressed() {
        fragmentSearchViewModel.storeToSHaredPreffs(this);
        fragmentFavCovidDataViewModel.storeToSHaredPreffs(this);
        finish();
    }
}