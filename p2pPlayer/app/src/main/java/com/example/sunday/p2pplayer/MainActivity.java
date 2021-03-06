package com.example.sunday.p2pplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.sunday.p2pplayer.downloaded.FragmentDownloaded;
import com.example.sunday.p2pplayer.downloading.FragmentDownloading;
import com.example.sunday.p2pplayer.model.MovieBean;
import com.example.sunday.p2pplayer.search.FragmentSearch;
import com.google.gson.reflect.TypeToken;
import com.gyf.immersionbar.ImmersionBar;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.vov.vitamio.Vitamio;

@Route(path = "showmovie/mainactivity")
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

       /* if (null instanceof MainActivity) {
            Log.d("MainActivity", "onCreate");
        }*/
     Log.d("MainActivity", "onCreate");



        ImmersionBar.with(this).navigationBarColor(R.color.colorPrimary).init();

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

         Observable.create(emitter -> {

        }).subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(new Observer<Object>() {
                     @Override
                     public void onSubscribe(Disposable d) {

                     }

                     @Override
                     public void onNext(Object o) {

                     }

                     @Override
                     public void onError(Throwable e) {

                     }

                     @Override
                     public void onComplete() {

                     }
                 });
        new TypeToken<List<MovieBean>>(){}.getType();
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
}
