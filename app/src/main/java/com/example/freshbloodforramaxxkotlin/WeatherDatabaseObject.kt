package com.example.freshbloodforramaxxkotlin

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class WeatherDatabaseObject : RealmObject() {
    @PrimaryKey
    var dt: Int = 0
    var temp: Double = 0.0
    var description: String = ""
    var main: String = ""
    var icon: String = ""


    fun getUrl() : String {
        return "http://openweathermap.org/img/wn/" + icon + "@2x.png"
    }

}