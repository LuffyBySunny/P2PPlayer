package com.example.sunday.p2pplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.sunday.p2pplayer.Util.UtilKt;
import com.example.sunday.p2pplayer.bittorrent.DownLoadManager;
import com.example.sunday.p2pplayer.downloaded.FragmentDownloaded;
import com.example.sunday.p2pplayer.downloading.FragmentDownloading;
import com.example.sunday.p2pplayer.search.FragmentSearch;
import com.gyf.immersionbar.ImmersionBar;
import com.leon.lfilepickerlibrary.utils.Constant;
import com.sunday.networklistener.NetType;
import com.sunday.networklistener.annotate.NetWork;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, ViewPager.OnPageChangeListener{

    public static final int PAGE_ONE = 0;
    public static final int PAGE_TWO = 1;
    public static final int PAGE_THREE = 2;

    private RadioButton mSearch;
    private RadioButton mDownloaded;
    public RadioButton mDownloading;


    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        ImmersionBar.with(this).navigationBarColor(R.color.colorPrimary).init();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        RadioGroup mRadiaGroup = findViewById(R.id.tab_bar);
        mSearch = findViewById(R.id.tab_search);
        mDownloading = findViewById(R.id.tab_downloading);
        mDownloaded = findViewById(R.id.tab_downloaded);
        mViewPager = findViewById(R.id.viewPager);
        mRadiaGroup.setOnCheckedChangeListener(this);

        mSearch.setChecked(true);
        FragmentSearch search = new FragmentSearch();
        FragmentDownloading downloading = new FragmentDownloading();
        FragmentDownloaded downloaded = new FragmentDownloaded();
        List<Fragment> list = new ArrayList<>();
        list.add(search);
        list.add(downloading);
        list.add(downloaded);

        downloading.setBitDownloadListener(downloaded);

        MyFragmentPageAdapter fragmentPageAdapter = new MyFragmentPageAdapter(getSupportFragmentManager(), list);
        mViewPager.setAdapter(fragmentPageAdapter);
        mViewPager.setCurrentItem(0);

        mViewPager.addOnPageChangeListener(this);

        downloadFile(getIntent());
    }





    @NetWork
    public void netWorkChanged(NetType netType) {
        if (netType == NetType.GPRS) {
            Toast.makeText(this, "4G",Toast.LENGTH_SHORT).show();
        } else if (netType == NetType.WIFI) {
            Toast.makeText(this, "WIFI",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        switch (checkedId) {
            case R.id.tab_search:
                mViewPager.setCurrentItem(PAGE_ONE);
                break;
            case R.id.tab_downloading:
                mViewPager.setCurrentItem(PAGE_TWO);
                break;
            case R.id.tab_downloaded:
                mViewPager.setCurrentItem(PAGE_THREE);
                break;
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        switch (i) {
            case PAGE_ONE:

                mSearch.setChecked(true);
                break;
            case PAGE_TWO:

                mDownloading.setChecked(true);
                break;
            case PAGE_THREE:

                mDownloaded.setChecked(true);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    @Override
    protected void onNewIntent(Intent intent) {

        downloadFile(intent);
        super.onNewIntent(intent);
    }

    private void downloadFile(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            //拿到数据
           Uri uri = intent.getData();
           if (uri != null) {
               //将Content换成file
               Uri newUri = UtilKt.getFilePath(this, uri);
               if (newUri != null) {
                   DownLoadManager.INSTANCE.downloadTorrent(newUri, "");
               }
           }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case UtilKt.REQUEST_CODE_FILEPICKER:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        ArrayList<String> files = data.getStringArrayListExtra(Constant.RESULT_INFO);
                        for (String file : files) {
                            Uri uri = Uri.parse(String.format("file://%s", file));
                            if (uri != null) {
                                DownLoadManager.INSTANCE.downloadTorrent(uri, "");
                            }
                        }
                    }
                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
