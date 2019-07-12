package com.example.freshbloodforramaxxkotlin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*


@Suppress("MemberVisibilityCanBePrivate")
class MyAdapter(val weatherList: List<WeatherDatabaseObject>, val context: Context) : androidx.recyclerview.widget.RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view  = LayoutInflater.from(p0.context).inflate(R.layout.item_layout ,p0, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return weatherList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val weather: WeatherDatabaseObject = weatherList[p1]

        val date = Date(weather.dt * 1000L)
        val locale = Locale("ru","RU")
        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm ",locale)
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+3"))
        val formattedDate = sdf.format(date)

        val temp = weather.temp - 273.15

        val formattedTemp = String.format("%.0f", temp)

        p0.temperatureView.text = formattedTemp
        p0.dateTimeView.text = formattedDate.toString()

        Glide
            .with(context)
            .load(weather.getUrl())
            .into(p0.imageView)
    }

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        val temperatureView: TextView = itemView.findViewById(R.id.temperature_view)
        val dateTimeView:TextView = itemView.findViewById(R.id.date_time_view)
    }

}

