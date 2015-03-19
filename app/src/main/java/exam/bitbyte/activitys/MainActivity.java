package exam.bitbyte.activitys;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import exam.bitbyte.R;
import exam.bitbyte.fragments.GoalFragment;
import exam.bitbyte.fragments.GraphFragment;
import exam.bitbyte.fragments.RankFragment;
import exam.bitbyte.fragments.TotalFragment;
import exam.bitbyte.fragments.YorkFragment;
import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;

public class MainActivity extends ActionBarActivity implements MaterialTabListener {

    private Toolbar toolbar;
    private MaterialTabHost tabHost;
    private DrawerLayout dlDrawer;
    private ActionBarDrawerToggle dtToggle;
    private ViewPager pager;
    private ViewPagerAdapter pagerAdapter;

    @Override
    protected void onResume(){
        super.onResume();
        CHECK();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //탭, 툴바 부분
        String[] tabName = {"나의 현황", "그래프", "욕설 현황", "목표", "바른말 랭킹"};
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dlDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        dtToggle = new ActionBarDrawerToggle(this, dlDrawer, R.string.app_name, R.string.app_name);
        dlDrawer.setDrawerListener(dtToggle);

        tabHost = (MaterialTabHost) this.findViewById(R.id.materialTabHost);
        pager = (ViewPager) this.findViewById(R.id.viewpager);

        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // when user do a swipe the selected tab change
                tabHost.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            tabHost.addTab(
                    tabHost.newTab()
                            .setText(tabName[i])
                            .setTabListener((it.neokree.materialtabs.MaterialTabListener) this)
            );
        }
    }

    void CHECK(){
        if (!(checkKeyboardEnable()))
        {
            AlertDialog.Builder localBuilder1 = new AlertDialog.Builder(this);
            localBuilder1.setMessage(Html.fromHtml("'언어 및 키보드'설정에서 <br>바른말 키패드를 활성화 합니다.<br>바른말 키패드를 설정한후 '뒤로가기'를 눌러주세요</font>"));
            localBuilder1.setPositiveButton("확인", this.EnableButtonClickListener);
            localBuilder1.setTitle("바른말 키패드 설정");
            localBuilder1.setCancelable(false);
            localBuilder1.show();
        }
        else if(!(checkKeyboardSetDefault()))
        {
            AlertDialog.Builder localBuilder2 = new AlertDialog.Builder(this);
            localBuilder2.setMessage(Html.fromHtml("입력 방법을<br>[바른말 키패드]로 설정해주세요"));
            localBuilder2.setPositiveButton("확인", this.DefaultButtonClickListener);
            localBuilder2.setTitle("바른말 키패드 설정");
            localBuilder2.setCancelable(false);
            localBuilder2.show();
        }
    }

    public boolean checkKeyboardEnable()
    {
        List localList = ((InputMethodManager)getApplicationContext().getSystemService(INPUT_METHOD_SERVICE)).getEnabledInputMethodList();
        for (int i = 0; ; i++)
        {
            if (i >= localList.size())
                return false;
            if (((InputMethodInfo)localList.get(i)).getId().equals("exam.bitbyte/.BareunmalKeypadService"))
                return true;
        }
    }

    public boolean checkKeyboardSetDefault()
    {
        String str = Settings.Secure.getString(getApplicationContext().getContentResolver(), "default_input_method");
        System.out.println("STRING : " + str);
        return (str != null) && (str.equals("exam.bitbyte/.BareunmalKeypadService"));
    }

    private DialogInterface.OnClickListener DefaultButtonClickListener = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface paramDialogInterface, int paramInt)
        {
            ((InputMethodManager)MainActivity.this.getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker();
        }
    };
    private DialogInterface.OnClickListener EnableButtonClickListener = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface paramDialogInterface, int paramInt)
        {
            //KeyboardMain.this.startActivity(new Intent("android.settings.INPUT_METHOD_SETTINGS"));
            Intent intentSubActivity =
                    new Intent(MainActivity.this, InputSetActivity.class);
            startActivity(intentSubActivity);
        }
    };

    //메뉴바 (왼쪽 상단 눌렀을때)와 관련된 부분
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        dtToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dtToggle.onConfigurationChanged(newConfig);
    }

    /*액션바*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        //#074D61  #054352
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (dtToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intentSubActivity1 = new Intent(MainActivity.this,
                        SettingsActivity.class);
                startActivity(intentSubActivity1);
                break;

            case R.id.menu_rank:
                Intent intentSubActivity2 = new Intent(MainActivity.this,
                        FacebookActivity.class);
                startActivity(intentSubActivity2);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        public Fragment getItem(int num) {
            switch (num){
                case 0:
                    return new TotalFragment();
                case 1:
                    return new GraphFragment();
                case 2:
                    return new YorkFragment();
                case 3:
                    return new GoalFragment();
                case 4:
                    return new RankFragment();
            }
            return null;
        }
        @Override
        public int getCount() {
            return 5;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return "tab";
        }
    }

    @Override
    public void onTabSelected(MaterialTab tab) {
        // when the tab is clicked the pager swipe content to the tab position
        pager.setCurrentItem(tab.getPosition());

    }

    @Override
    public void onTabReselected(MaterialTab materialTab) {

    }

    @Override
    public void onTabUnselected(MaterialTab materialTab) {

    }
}