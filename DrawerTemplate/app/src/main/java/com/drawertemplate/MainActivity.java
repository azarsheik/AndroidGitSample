package com.drawertemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.drawertemplate.fragments.ViewFragment;
import com.drawertemplate.fragments.WebViewFragment;
import com.drawertemplate.utils.Consts;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView title;
    private DrawerLayout drawer;
    private static int mCurrentSelectedPosition = R.id.nav_option1;
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
        }

        title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        }

        navigationView.setCheckedItem(mCurrentSelectedPosition);

        populateFragments(loadFragments(mCurrentSelectedPosition));

        ((TextView) findViewById(R.id.text_footer)).setText(String.format(getString(R.string.nav_item_footer),
                BuildConfig.VERSION_NAME));
    }

    private void populateFragments(final Fragment fragment) {
        if (fragment != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.flContent);
        if (fragment == null) {
            return;
        }

        if (fragment instanceof WebViewFragment && ((WebViewFragment) fragment).canGoBack()) {
            ((WebViewFragment) fragment).goBack();
        } else if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        mCurrentSelectedPosition = id;

        populateFragments(loadFragments(id));
        return true;
    }

    private Fragment loadFragments(int id) {
        Fragment fragment = null;

        if (id == R.id.nav_option1) {
            title.setText(getString(R.string.nav_item1));
            fragment = WebViewFragment.newInstance(Consts.AboutConsts.ABOUT_URL);
        } else if (id == R.id.nav_option2) {
            title.setText(getString(R.string.nav_item2));
            fragment = WebViewFragment.newInstance(Consts.AboutConsts.ABOUT_URL2);
        } else if (id == R.id.nav_option3) {
            title.setText(getString(R.string.nav_item3));
            fragment = ViewFragment.newInstance();
        }

        drawer.closeDrawer(GravityCompat.START);

        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

}
