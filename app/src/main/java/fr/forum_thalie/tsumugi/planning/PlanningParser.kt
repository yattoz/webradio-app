package fr.forum_thalie.tsumugi.planning

import android.content.Context
import fr.forum_thalie.tsumugi.Async
import org.json.JSONObject
import java.io.IOException
import java.net.URL

class PlanningParser {

    val programs: ArrayList<Program> = ArrayList()

    fun parseUrl(url: String? = null, context: Context? = null)
    {
        val scrape : (Any?) -> String = {
            if (url.isNullOrEmpty() && context != null)
            {
                val json: String
                try {
                    val inputStream = context.assets.open("planning_example.json")
                    val size = inputStream.available()
                    val buffer = ByteArray(size)
                    inputStream.use { it.read(buffer) }
                    json = String(buffer)
                    json
                } catch (ioException: IOException) {
                    ioException.printStackTrace()
                    ""
                }
            }
            else
                URL(url).readText()

        }
        val post : (parameter: Any?) -> Unit = {
            val result = JSONObject(it as String)
            if (result.has("planning"))
            {
                val programList = result.getJSONArray("planning")
                for (i in 0 until programList.length())
                {
                    val item = programList[i] as JSONObject
                    val periodicity = item.getInt("periodicity")
                    val hourBeginS = item.getString("hour_begin").split(":")
                    val hourBegin = hourBeginS.first().toInt()*60 + hourBeginS.last().toInt()
                    val hourEndS = item.getString("hour_end").split(":")
                    val hourEnd = hourEndS.first().toInt()* 60 + hourEndS.last().toInt()
                    val title = item.getString("title")
                    programs.add(Program(title, periodicity, hourBegin, hourEnd))
                }
            }
        }
        Async(scrape, post)
    }

    companion object {
        val instance by lazy {
            PlanningParser()
        }
    }
}