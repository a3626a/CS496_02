package kaist.cs496_02;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.astuetz.PagerSlidingTabStrip;

public class MainActivity extends FragmentActivity {

    public static String server_url_phone = "http://ec2-52-78-73-98.ap-northeast-2.compute.amazonaws.com:8080";
    public static String server_url_gallery = "http://ec2-52-78-73-98.ap-northeast-2.compute.amazonaws.com:8081";
    TabPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDemoCollectionPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(mViewPager);
    }
}



