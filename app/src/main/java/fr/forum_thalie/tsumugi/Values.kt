package fr.forum_thalie.tsumugi

import android.content.SharedPreferences
import android.content.res.ColorStateList
import kotlin.collections.ArrayList

const val tag = "fr.forum_thalie.tsumugi"
const val noConnectionValue = "—"
const val streamDownValue = "Tsumugi est HS !" // we don't want this value to be displaed in the "last played" screen.
val weekdaysArray : Array<String> = arrayOf( "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche")

// Below this line is only automatically programmed values. Unless your week does not start with Monday, you don't need to change this.

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