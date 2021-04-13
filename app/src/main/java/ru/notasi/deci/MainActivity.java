package ru.notasi.deci;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView mBottomNav;
    private Toast mToast;
    private HideBottomViewOnScrollBehavior<BottomNavigationView> mScrollBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Repository repo = new Repository(this);
//        repo.useLang(repo.getLang()); TODO: Fix lang.
        repo.useTheme(repo.getTheme());
        setContentView(R.layout.activity_main);

        mBottomNav = (findViewById(R.id.bottom_nav));
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(mBottomNav, navController);
//        mBottomNav.getOrCreateBadge(R.id.nav_info).setNumber(1); TODO: Updates.
        mBottomNav.setOnNavigationItemReselectedListener(item -> {
        }); // Override.

        ViewGroup.LayoutParams layoutParams = mBottomNav.getLayoutParams();
        if (layoutParams instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.Behavior behavior =
                    ((CoordinatorLayout.LayoutParams) layoutParams).getBehavior();
            if (behavior instanceof HideBottomViewOnScrollBehavior) {
                mScrollBehavior =
                        (HideBottomViewOnScrollBehavior<BottomNavigationView>) behavior;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        slideNavUp();
    }

    public void slideNavUp() {
        mScrollBehavior.slideUp(mBottomNav);
    }

    public void slideNavDown() {
        mScrollBehavior.slideDown(mBottomNav);
    }

    public int getBadges(int menu) {
        return mBottomNav.getOrCreateBadge(menu).getNumber();
    }

    public void setBadges(int menu, int badges) {
        mBottomNav.getOrCreateBadge(menu).setNumber(badges);
    }

    public void decreaseBadges(int menu) {
        int badges = mBottomNav.getOrCreateBadge(menu).getNumber();
        if (badges > 1) {
            mBottomNav.getOrCreateBadge(menu).setNumber(--badges);
        } else {
            mBottomNav.removeBadge(menu);
        }
    }

    public void removeBadges(int menu) {
        mBottomNav.removeBadge(menu);
    }

    public String getVersion() {
        String version = Constants.TEXT_ZERO;
        try {
            version = getPackageManager()
                    .getPackageInfo(getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public void showToast(String text) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        mToast.show();
    }
}