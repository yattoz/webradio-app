package fr.forum_thalie.tsumugi

import android.content.SharedPreferences
import android.content.res.ColorStateList
import kotlin.collections.ArrayList

const val tag = "fr.forum_thalie.tsumugi"
const val noConnectionValue = "Arrêté."
const val streamDownValue = "Tsumugi est HS !"
val weekdaysArray : Array<String> = arrayOf( "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche")

val weekdays = ArrayList<String>().apply { weekdaysArray.forEach { add(it) } }
val weekdaysSundayFirst = ArrayList<String>().apply {
    weekdays.forEach {
        add(it)
    }
    val lastDay = last()
    removeAt(size - 1)
    add(0, lastDay)
}

var colorBlue: Int = 0
var colorWhited: Int = 0
var colorAccent : Int = 0
var colorGreenList: ColorStateList? = ColorStateList.valueOf(0)
var colorRedList: ColorStateList? = ColorStateList.valueOf(0)
var colorGreenListCompat : ColorStateList? = ColorStateList.valueOf(0)
lateinit var preferenceStore : SharedPreferences