package fr.forum_thalie.tsumugi.planning

import android.util.Log
import fr.forum_thalie.tsumugi.tag
import java.util.*

class Programme (val title: String, private val periodicity: Int, private val hourBegin: Int, private val hourEnd: Int) {
    fun isCurrent(): Boolean {
        val now = Calendar.getInstance()
        val currentDay =
            if (now.get(Calendar.DAY_OF_WEEK) - 1 == 0) 6 else now.get(Calendar.DAY_OF_WEEK) - 2
        // 0 (Monday) to 5 (Saturday) + 6 (Sunday)

        // this translates to "true" when:
        // - the currentDay is flagged in the "periodicity" bit array
        // OR
        // - Yesterday is flagged in the "periodicity" bit array AND the program does span over 2 days (night programs).
        // We'll check a after this whether the current hour is within the span.
        val isToday: Boolean = ((((1000000 shr currentDay) and (periodicity)) != 0))
        val isSpanningOverNight =
            (((1000000 shr ((currentDay - 1) % 7) and (periodicity)) != 0) && hourEnd < hourBegin)

        Log.d(tag, "$title is today: $isToday or spanning $isSpanningOverNight")
        // shr = shift-right. It's a binary mask.

        // if the program started yesterday, and spanned over night, it means that there could be a chance that it's still active.
        // we only need to check if the end time has been reached.
        if (isSpanningOverNight) {
            return (now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE) < hourEnd)
        }

        // if the program is today, we need to check if we're in the hour span.
        if (isToday) {
            val hasBegun =
                (now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE) >= hourBegin)
            val hasNotEnded =
                (now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE) < hourEnd) || hourEnd < hourBegin
            return (hasBegun && hasNotEnded)
        }
        return false
    }

    override fun toString(): String {
        return "Title: $title, time info (periodicity, begin, end): $periodicity, $hourBegin, $hourEnd"
    }

    init {
        Log.d(tag, this.toString())
    }
}