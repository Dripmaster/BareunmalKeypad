package exam.bitbyte.activitys;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import exam.bitbyte.R;
import exam.bitbyte.fragments.Tutorial1Fragment;
import exam.bitbyte.fragments.Tutorial2Fragment;
import exam.bitbyte.fragments.Tutorial3Fragment;
import exam.bitbyte.fragments.Tutorial4Fragment;
import exam.bitbyte.fragments.Tutorial5Fragment;
import exam.bitbyte.fragments.Tutorial6Fragment;

public class TutorialActivity extends ActionBarActivity {

    private ViewPager pager;
    private ViewPagerAdapter pagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_layout);

        pager = (ViewPager) this.findViewById(R.id.viewpagerT);
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // when user do a swipe the selected tab change
            }
        });
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        public Fragment getItem(int num) {
            switch (num){
                case 0:
                    return new Tutorial1Fragment();
                case 1:
                    return new Tutorial2Fragment();
                case 2:
                    return new Tutorial3Fragment();
                case 3:
                    return new Tutorial4Fragment();
                case 4:
                    return new Tutorial5Fragment();
                case 5:
                    return new Tutorial6Fragment();
            }
            return null;
        }
        @Override
        public int getCount() {
            return 6;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return "tab";
        }
    }
}