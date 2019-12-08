package fr.forum_thalie.tsumugi

import android.content.SharedPreferences
import android.content.res.ColorStateList

const val tag = "fr.forum_thalie.tsumugi"
const val noConnectionValue = "No connection"
const val streamDownValue = "Tsumugi est HS !"
var colorBlue: Int = 0
var colorWhited: Int = 0
var colorGreenList: ColorStateList? = ColorStateList.valueOf(0)
var colorRedList: ColorStateList? = ColorStateList.valueOf(0)
var colorGreenListCompat : ColorStateList? = ColorStateList.valueOf(0)

lateinit var preferenceStore : SharedPreferences