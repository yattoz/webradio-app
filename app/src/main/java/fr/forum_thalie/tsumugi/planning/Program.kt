package fr.forum_thalie.tsumugi.planning

import android.util.Log
import fr.forum_thalie.tsumugi.tag
import org.json.JSONObject
import java.util.*

class Program (val title: String, private val periodicity: Int, private val hourBegin: Int, private val hourEnd: Int) {
    fun isCurrent(): Boolean
    {
        val now = Calendar.getInstance()
        val currentDay = if (now.get(Calendar.DAY_OF_WEEK) - 1 == 0) 6 else now.get(Calendar.DAY_OF_WEEK) - 2
        // 0 (Monday) to 5 (Saturday) + 6 (Sunday)

        // this translates to "true" when:
        // - the currentDay is flagged in the "periodicity" bit array
        // OR
        // - Yesterday is flagged in the "periodicity" bit array AND the program does span over 2 days (night programs typically).
        // We'll check a after this whether the current hour is within the span.
        val isNow: Boolean = ((((1000000 shr currentDay) and (periodicity)) == 1)) || (((1000000 shr ((currentDay-1)%7) and (periodicity)) == 1) && hourEnd < hourBegin)

        // shr = shift-right. It's a binary mask.
        if (isNow)
        {
            val hasBegun = (now.get(Calendar.HOUR_OF_DAY)*60 + now.get(Calendar.MINUTE) >= hourBegin )
            val hasNotEnded = (now.get(Calendar.HOUR_OF_DAY)*60 + now.get(Calendar.MINUTE) <= hourEnd )
            if (hasBegun && hasNotEnded)
                return true
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