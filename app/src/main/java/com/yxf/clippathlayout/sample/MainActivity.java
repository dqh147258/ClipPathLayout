package com.yxf.clippathlayout.sample;

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

import com.yxf.clippathlayout.pathgenerator.CirclePathGenerator;
import com.yxf.clippathlayout.pathgenerator.OvalPathGenerator;
import com.yxf.clippathlayout.pathgenerator.RhombusPathGenerator;
import com.yxf.clippathlayout.transition.TransitionAdapter;
import com.yxf.clippathlayout.transition.TransitionFragmentContainer;
import com.yxf.clippathlayout.transition.generator.RandomTransitionPathGenerator;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private TransitionFragmentContainer mContainer;

    private WeakReference<Fragment> mLastFragmentReference;

    FragmentManager mFragmentManager = getSupportFragmentManager();

    private TransitionAdapter mTransitionAdapter;

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
                new RandomTransitionPathGenerator(new CirclePathGenerator());
        generator.add(new OvalPathGenerator());
        generator.add(new RhombusPathGenerator());
        mTransitionAdapter = new TransitionAdapter(generator);
        mTransitionAdapter.setImmediately(true);
        mContainer.setAdapter(mTransitionAdapter);
        switchFragment(new ScrollTransitionFragment(), false);
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
        if (mTransitionAdapter.isImmediately()) {
            mTransitionAdapter.setImmediately(false);
        }
        switchFragment(fragment, true);
    }

    private void switchFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        Fragment f;
        if (mLastFragmentReference != null && (f = mLastFragmentReference.get()) != null) {
            transaction.hide(f);
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.add(R.id.fragment_container, fragment).commit();
        mLastFragmentReference = new WeakReference<Fragment>(fragment);
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
