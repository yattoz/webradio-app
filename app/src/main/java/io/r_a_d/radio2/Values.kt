package io.r_a_d.radio2

import android.content.SharedPreferences
import android.content.res.ColorStateList

const val tag = "io.r_a_d.radio2"
const val noConnectionValue = "No connection"
var colorBlue: Int = 0
var colorWhited: Int = 0
var colorGreenList: ColorStateList? = ColorStateList.valueOf(0)
var colorRedList: ColorStateList? = ColorStateList.valueOf(0)
var colorGreenListCompat : ColorStateList? = ColorStateList.valueOf(0)

lateinit var preferenceStore : SharedPreferences