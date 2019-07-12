package com.example.freshbloodforramaxxkotlin

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.dialog_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mViewAdapter: RecyclerView.Adapter<*>
    private lateinit var mViewManager: RecyclerView.LayoutManager

    private lateinit var mDialog: Dialog

    private lateinit var mRealm: Realm
    private lateinit var mConfig: RealmConfiguration

    private lateinit var mContext: Context

    private var mWeatherDatabaseObjectList: ArrayList<WeatherDatabaseObject> = ArrayList()

    private lateinit var mUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContext = this

        Realm.init(this)
        mConfig = RealmConfiguration.Builder().name("weather_database.realm").build()
        mRealm = Realm.getInstance(mConfig)

        mRecyclerView = findViewById(R.id.recycler_view)
        mProgressBar = findViewById(R.id.progress_circular)

        mProgressBar.visibility = View.GONE

        mViewManager = LinearLayoutManager(this)
        mRecyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = mViewManager

            addOnItemTouchListener(
                RecyclerItemClickListener(
                    mContext,
                    mRecyclerView,
                    object : RecyclerItemClickListener.OnItemClickListener {
                        override fun onItemClick(view: View, position: Int) {
                            val buff = mWeatherDatabaseObjectList[position]
                            mUrl = buff.getUrl()
                            showDialog(buff.main, buff.description, buff.getUrl())
                        }

                        override fun onItemLongClick(view: View?, position: Int) {

                        }
                    })
            )
        }
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        Log.d("TAG", "onSave")
        // saving recyclerView state
        val recyclerViewState = mViewManager.onSaveInstanceState()
        outState?.putParcelable("recycler_view_state", recyclerViewState)

        if (::mUrl.isInitialized) outState?.putCharSequence("url", mUrl)

        // saving dialog data
        if (::mDialog.isInitialized && mDialog.isShowing) {
            mDialog.cancel()
            outState?.putBoolean("dialog_state", true)
            outState?.putCharSequence("main", mDialog.dialog_title.text)
            outState?.putCharSequence("url", mUrl)
            outState?.putCharSequence("description", mDialog.dialog_description.text)

        } else {
            log("mDialog.isInitialized = false")
        }

        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        Log.d("TAG", "onRestore ")
        super.onRestoreInstanceState(savedInstanceState)
        // restoring recyclerView state
        val listState = savedInstanceState?.getParcelable<Parcelable>("recycler_view_state")
        mViewManager.onRestoreInstanceState(listState)

        val dialogIsShowing = savedInstanceState?.getBoolean("dialog_state")

        val url = savedInstanceState?.getCharSequence("url").toString()
        // may be null
        mUrl = url
        // but if dialog is showing, it couldn't
        // main and description exists 100%
        if (dialogIsShowing == true) {
            val main = savedInstanceState.getCharSequence("main")
            val description = savedInstanceState.getCharSequence("description")
            showDialog(main!!.toString(), description!!.toString(), mUrl)
        }
    }


    override fun onPause() {
        log("onPause")
        super.onPause()
    }

    override fun onResume() {
        log("onResume")
        getData()
        super.onResume()
    }

    fun setAdapterToRecyclerView(list: List<WeatherDatabaseObject>, context: Context): Boolean {
        mViewAdapter = MyAdapter(list, context)
        mRecyclerView.adapter = mViewAdapter
        return true
    }


    companion object {
        var BaseUrl = "http://samples.openweathermap.org/"
        var AppId = "fcd64cf4aa18478184f7e912c0ca9482"
        var ID = 524894
    }

    private fun getData() {
        log("getData()")

        // check if dataBase exists
        val realmRes = mRealm.where(WeatherDatabaseObject::class.java).findAll()

        if (realmRes.size > 0) {
            for (i in 0 until realmRes.size) {
                val weatherDatabaseObjectBuff = realmRes[i]
                mWeatherDatabaseObjectList.add(weatherDatabaseObjectBuff!!)
            }
            setAdapterToRecyclerView(mWeatherDatabaseObjectList, mContext)
            return
        } else {
            mProgressBar.visibility = View.VISIBLE

            val retrofit = Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val service = retrofit.create(WeatherService::class.java)

            val call = service.getCurrentWeatherData(AppId, ID)

            call.enqueue(object : Callback<WeatherResp> {
                override fun onResponse(call: Call<WeatherResp>, response: Response<WeatherResp>) {
                    if (response.code() == 200) {
                        val weatherResp = response.body()
                        if (weatherResp!!.cnt > 0) {
                            // taking list from responce
                            val itemList = weatherResp.list
                            // creating new List<WeatherDatabaseObject>
                            // and pass it to adapter
                            for (i in 0 until itemList!!.size) {
                                val buffer = itemList[i]

                                val dt = buffer.dt
                                val temp = buffer.main.temp

                                try {
                                    val weather = buffer.weather!![0]
                                    val main = weather.main
                                    val description = weather.description
                                    val icon = weather.icon

                                    val weatherDatabaseObjectBuff = WeatherDatabaseObject()

                                    weatherDatabaseObjectBuff.dt = dt
                                    weatherDatabaseObjectBuff.temp = temp
                                    weatherDatabaseObjectBuff.main = main
                                    weatherDatabaseObjectBuff.description = description
                                    weatherDatabaseObjectBuff.icon = icon

                                    mWeatherDatabaseObjectList.add(weatherDatabaseObjectBuff)

                                    mRealm.beginTransaction()
                                    mRealm.copyToRealmOrUpdate(weatherDatabaseObjectBuff)
                                    mRealm.commitTransaction()

                                    mProgressBar.visibility = View.GONE
                                } catch (e: IndexOutOfBoundsException) {
                                    log("error on :" + i + "" + e.message)
                                }
                            }
                            setAdapterToRecyclerView(mWeatherDatabaseObjectList, mContext)
                        }
                    }
                }


                override fun onFailure(call: Call<WeatherResp>, t: Throwable) {
                    log("onFailure: \n " + t.localizedMessage)
                    mProgressBar.visibility = View.GONE
                    makeToast("Failed to get data")
                }
            })

        }

    } //getData()

    fun makeToast(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    private fun showDialog(title: String, description: String, url: String) {
        mDialog = Dialog(mContext)
        mDialog.setCancelable(true)
        mDialog.setCanceledOnTouchOutside(true)
        mDialog.setContentView(R.layout.dialog_layout)

        val titleView = mDialog.findViewById(R.id.dialog_title) as TextView
        titleView.text = title

        val descriptionView = mDialog.findViewById(R.id.dialog_description) as TextView
        descriptionView.text = description

        val imageView = mDialog.findViewById(R.id.dialog_image_view) as ImageView
        Glide
            .with(mContext)
            .load(url)
            .into(imageView)

        mDialog.show()
    }

    private fun log(message: String) {
        Log.d("TAG", message)
    }
}
