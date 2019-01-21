package com.yxf.clippathlayout.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.yxf.clippathlayout.transition.TransitionAdapter;
import com.yxf.clippathlayout.transition.TransitionFragmentContainer;
import com.yxf.clippathlayout.transition.generator.CircleTransitionPathGenerator;
import com.yxf.clippathlayout.transition.generator.OvalTransitionPathGenerator;
import com.yxf.clippathlayout.transition.generator.RandomTransitionPathGenerator;
import com.yxf.clippathlayout.transition.generator.RhombusTransitionPathGenerator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private TransitionFragmentContainer mContainer;

    private Fragment mLastFragment;

    FragmentManager mFragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mContainer = findViewById(R.id.fragment_container);
        RandomTransitionPathGenerator generator =
                new RandomTransitionPathGenerator(new CircleTransitionPathGenerator());
        generator.add(new OvalTransitionPathGenerator());
        generator.add(new RhombusTransitionPathGenerator());
        mContainer.setAdapter(new TransitionAdapter(generator));
        switchFragment(new ScrollTransitionFragment());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (mFragmentManager.getBackStackEntryCount() > 0) {
                mFragmentManager.popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (mLastFragment != null) {
            transaction.hide(mLastFragment);
            transaction.addToBackStack(null);
        }
        transaction.add(R.id.fragment_container, fragment).commit();
        mLastFragment = fragment;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_circle_path) {
            switchFragment(new CirclePathFragment());
        } else if (id == R.id.nav_control_button) {
            switchFragment(new ControlButtonFragment());
        } else if (id == R.id.nav_remote_controller) {
            switchFragment(new RemoteControllerFragment());
        } else if (id == R.id.yin_yang_fish) {
            switchFragment(new YinYangFishFragment());
        } else if (id == R.id.nav_view_transition) {
            switchFragment(new ViewTransitionFragment());
        } else if (id == R.id.nav_scroll_transition) {
            switchFragment(new ScrollTransitionFragment());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
