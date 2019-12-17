package fr.forum_thalie.tsumugi.ui.songs.programme

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import fr.forum_thalie.tsumugi.R
import fr.forum_thalie.tsumugi.colorBlue
import fr.forum_thalie.tsumugi.colorWhited
import fr.forum_thalie.tsumugi.planning.Programme
import fr.forum_thalie.tsumugi.tag
import kotlinx.android.synthetic.main.programme_view.view.*
import kotlinx.android.synthetic.main.song_view.view.*

class ProgrammeAdapter(private val dataSet: ArrayList<Programme>
    /*,
    context: Context,
    resource: Int,
    objects: Array<out Song>*/
) : RecyclerView.Adapter<ProgrammeAdapter.MyViewHolder>() /*ArrayAdapter<Song>(context, resource, objects)*/ {

    class MyViewHolder(view: ConstraintLayout) : RecyclerView.ViewHolder(view)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.programme_view, parent, false) as ConstraintLayout
        return MyViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val programmeStart = holder.itemView.findViewById<TextView>(R.id.programme_start)
        val programmeEnd = holder.itemView.findViewById<TextView>(R.id.programme_end)
        val programmeName = holder.itemView.findViewById<TextView>(R.id.programme_name)
        val programmeDays = holder.itemView.findViewById<TextView>(R.id.programme_days)

        programmeStart.text = dataSet[position].begin()
        programmeName.text = dataSet[position].title
        programmeEnd.text = dataSet[position].end()
        programmeDays.text = dataSet[position].days()
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