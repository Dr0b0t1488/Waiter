package com.example.waiter.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.waiter.R;
import com.example.waiter.viewmodels.AuthViewModel;
import com.example.waiter.viewmodels.WaiterViewModel;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        WaiterViewModel waiterViewModel = new ViewModelProvider(this).get(WaiterViewModel.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.loginFragment, R.id.tablesFragment, R.id.adminFragment, R.id.hyperAdminFragment)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                waiterViewModel.setEstablishmentCode(user.getEstablishmentCode());
            }
            invalidateOptionsMenu();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logoutItem = menu.findItem(R.id.action_logout);
        logoutItem.setVisible(authViewModel.getCurrentUser().getValue() != null);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            authViewModel.logout();
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.loginFragment);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
