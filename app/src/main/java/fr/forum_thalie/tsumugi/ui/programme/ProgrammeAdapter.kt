package fr.forum_thalie.tsumugi.ui.programme

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import fr.forum_thalie.tsumugi.R
import fr.forum_thalie.tsumugi.colorBlue
import fr.forum_thalie.tsumugi.planning.Programme
import fr.forum_thalie.tsumugi.weekdays
import fr.forum_thalie.tsumugi.weekdaysSundayFirst
import java.util.*
import kotlin.collections.ArrayList

class ProgrammeAdapter(private val dataSet: ArrayList<Programme>, private val day: String
    /*,
    context: Context,
    resource: Int,
    objects: ArrayList<Programme>*/
) : RecyclerView.Adapter<ProgrammeAdapter.MyViewHolder>() /*ArrayAdapter<Song>(context, resource, objects)*/ {

    class MyViewHolder(view: ConstraintLayout) : RecyclerView.ViewHolder(view)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.programme_view, parent, false) as ConstraintLayout
        return MyViewHolder(
            view
        )
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val programmeStart = holder.itemView.findViewById<TextView>(R.id.programme_start)
        val programmeEnd = holder.itemView.findViewById<TextView>(R.id.programme_end)
        val programmeName = holder.itemView.findViewById<TextView>(R.id.programme_name)

        programmeStart.text = dataSet[position].begin()
        programmeName.text = dataSet[position].title
        programmeEnd.text = dataSet[position].end()

        if (dataSet[position].isCurrent() && (Calendar.getInstance(TimeZone.getTimeZone("GMT+1")).get(Calendar.DAY_OF_WEEK) - 1 == weekdaysSundayFirst.indexOf(day)))
        {
            programmeStart.setTextColor(colorBlue)
            programmeEnd.setTextColor(colorBlue)
            programmeName.setTextColor(colorBlue)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size


    /*
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.song_view, parent, false) as ConstraintLayout
    }
    */
}