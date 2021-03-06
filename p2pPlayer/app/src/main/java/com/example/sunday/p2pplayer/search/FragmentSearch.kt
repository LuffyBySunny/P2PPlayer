package com.example.sunday.p2pplayer.search

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.sunday.p2pplayer.MainActivity
import com.example.sunday.p2pplayer.R
import com.example.sunday.p2pplayer.Util.*
import com.example.sunday.p2pplayer.bittorrent.DownLoadManager
import com.example.sunday.p2pplayer.model.MovieBean
import com.example.sunday.p2pplayer.movieplay.ExoVideoPlayer
import com.example.sunday.p2pplayer.movieplay.VVideoActivity
import com.example.sunday.p2pplayer.movieplay.VideoActivity
import com.example.sunday.p2pplayer.transfer.TransferManager
import com.gyf.immersionbar.ImmersionBar
import java.util.*
import java.util.regex.Pattern


/**
 * Created by Sunday on 2019/4/15
 */
class FragmentSearch : Fragment() {



    private lateinit var mViewModel : MyViewModel

    private lateinit var mLoadingView : LinearLayout

    private val mList = ArrayList<MovieBean>()

    private lateinit var editText : EditText
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mViewModel = ViewModelProviders.of(this).get(MyViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val toolbar = view.findViewById<Toolbar>(R.id.toolBar)
        val viewStub = view.findViewById<ViewStub>(R.id.viewStub)
        viewStub.inflate()
        mLoadingView = view.findViewById(R.id.LoadingView)

        (activity as MainActivity).setSupportActionBar(toolbar)

        ImmersionBar.setTitleBar(activity,toolbar)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        val mAdapter = SearchAdapter()
        recyclerView.adapter = mAdapter

        mViewModel.liveData.observe(this, Observer {
           mList.clear()
           if (it != null) {
               mList.addAll(it)
           }
            mLoadingView.visibility = View.GONE
           mAdapter.notifyDataSetChanged()
        })
       editText = view.findViewById(R.id.search_editText)
        editText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                (editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(activity!!.currentFocus!!.windowToken
                                , InputMethodManager.HIDE_NOT_ALWAYS)
                mLoadingView.visibility = View.VISIBLE
                mViewModel.search(editText.text.toString().trim(), context)
                return@OnEditorActionListener true
            }
            return@OnEditorActionListener false
        } )
        return view
    }


   inner class SearchAdapter : RecyclerView.Adapter<SearchAdapter.ViewHolder>(){


        private lateinit var mContext: Context

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            mContext = p0.context
            val view = LayoutInflater.from(mContext).inflate(R.layout.item_movie, p0, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {

            return mList.size
        }

        override fun onBindViewHolder(p0:ViewHolder, p1: Int) {

            Glide.with(mContext).load(mList[p1].imagePathString).
                    placeholder(R.drawable.noimage).
                    transform(CenterCrop(), RoundedCorners(10)).
                    into(p0.imageView)
            //后端返回的文字
            val spannable = SpannableString(mList[p1].name)
            //需要高亮显示的文字
            val pattern = Pattern.compile(this@FragmentSearch.editText.text.toString().trim())
            val matcher = pattern.matcher(mList[p1].name)
            while (matcher.find()) {
                val span = ForegroundColorSpan(activity?.let { ContextCompat.getColor(it, R.color.colorPrimary) }!!)
                spannable.setSpan(span, matcher.start() , matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            p0.movieDetails.text = mList[p1].details
            p0.movieName.text = spannable
            p0.downloadButton.setOnClickListener({
                PermissionUtil.requestPermission(activity!!,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE),
                        object : PermissionUtil.IPermissionListener {
                            override fun permissionDeny() {
                                Toast.makeText(activity,"拒绝了权限，无法下载",Toast.LENGTH_SHORT).show()
                            }
                            override fun permissionGranted() {
                                TransferManager.bitTorrentDownloads.forEach {
                                    val dataSourcePath = mList[p0.adapterPosition].dataSourcePath;
                                    val displayName = dataSourcePath.substring(dataSourcePath.lastIndexOf('/') + 1)
                                    if (TextUtils.equals(displayName, it.name)) {
                                        Toast.makeText(activity,"任务已存在",Toast.LENGTH_SHORT).show()
                                        return
                                    }
                                }
                                //添加下载的动画
                                addDownloadAnimation(p0.downloadButton,
                                            (activity as MainActivity).mDownloading,
                                            activity as MainActivity)
                                DownLoadManager.downloadTorrent(mList[p0.adapterPosition].torrentPathString, mList[p0.adapterPosition].name)
                            }
                        }, this@FragmentSearch)
            })

            p0.playButton.setOnClickListener({
                PermissionUtil.requestPermission(activity!!,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE),
                        object : PermissionUtil.IPermissionListener {
                            override fun permissionDeny() {
                                Toast.makeText(activity,"拒绝了权限，无法播放",Toast.LENGTH_SHORT).show()
                            }
                            override fun permissionGranted() {
                                val intent = Intent(activity, VideoActivity::class.java)
                                val dataSourcePath = mList[p0.adapterPosition].dataSourcePath
                                intent.putExtra(MOVIE_URL, dataSourcePath)
                                intent.putExtra(MOVIE_NAME, dataSourcePath.substring(dataSourcePath.lastIndexOf('/') + 1))
                                activity?.startActivity(intent)
                            }
                        }, this@FragmentSearch)
            })
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView = view.findViewById<ImageView>(R.id.movie_cav)!!
            val movieName = view.findViewById<TextView>(R.id.movie_title)!!
            val movieDetails = view.findViewById<TextView>(R.id.movie_detail)!!
            val playButton = view.findViewById<Button>(R.id.movie_play)!!
            val downloadButton = view.findViewById<Button>(R.id.movie_download)!!

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        PermissionUtil.onResultPermission(requestCode, permissions, grantResults)
    }


}