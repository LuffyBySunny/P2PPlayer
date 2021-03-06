package com.example.sunday.p2pplayer

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.sunday.p2pplayer.downloaded.FragmentDownloaded
import com.example.sunday.p2pplayer.downloading.FragmentDownloading
import com.example.sunday.p2pplayer.search.FragmentSearch

/**
 * Created by Sunday on 2019/4/15
 */
class MyFragmentPageAdapter(fm:FragmentManager, list: List<Fragment>) : FragmentPagerAdapter(fm) {

    private val fragments = list
    override fun getItem(p0: Int): Fragment {
       return fragments[p0]
    }

    override fun getCount(): Int {
        return fragments.size
    }


}