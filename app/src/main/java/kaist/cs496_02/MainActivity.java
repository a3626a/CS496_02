package kaist.cs496_02;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

public class MainActivity extends FragmentActivity {

    public static String server_url_phone = "http://ec2-52-78-73-98.ap-northeast-2.compute.amazonaws.com:8080";
    public static String server_url_gallery = "http://ec2-52-78-73-98.ap-northeast-2.compute.amazonaws.com:8081";
    TabPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get permission for contacts
        int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            Toast.makeText(getApplicationContext(), "permission requested", Toast.LENGTH_LONG);
        }
        int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 202;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            Toast.makeText(getApplicationContext(), "permission requested", Toast.LENGTH_LONG);
        }

        setContentView(R.layout.activity_main);
        mDemoCollectionPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(mViewPager);
    }

}



