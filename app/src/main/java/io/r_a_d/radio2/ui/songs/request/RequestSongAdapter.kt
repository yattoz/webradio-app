package io.r_a_d.radio2.ui.songs.request

import android.annotation.SuppressLint
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import io.r_a_d.radio2.*
import io.r_a_d.radio2.playerstore.Song
import kotlinx.android.synthetic.main.request_song_view.view.*
import android.view.View
import kotlinx.android.synthetic.main.button_load_more.view.*
import android.R.attr.name
import android.text.method.TextKeyListener.clear
import android.util.Log
import java.util.*
import kotlin.collections.ArrayList


class RequestSongAdapter(private val dataSet: ArrayList<Song>
    /*,
    context: Context,
    resource: Int,
    objects: Array<out Song>*/
) : RecyclerView.Adapter<RequestSongAdapter.MyViewHolder>() /*ArrayAdapter<Song>(context, resource, objects)*/ {

    private val viewTypeCell = 1 // normal cell with song and request button
    private val viewTypeFooter = 2 // the bottom cell should be the "load more" button whenever needed

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(view: ConstraintLayout) : RecyclerView.ViewHolder(view)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view

        val view =
            if (viewType == viewTypeCell)
                LayoutInflater.from(parent.context).inflate(R.layout.request_song_view, parent, false) as ConstraintLayout
            else
                LayoutInflater.from(parent.context).inflate(R.layout.button_load_more, parent, false) as ConstraintLayout

        // set the view's size, margins, paddings and layout parameters
        //...
        return MyViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        if (itemCount <= 1)
        {
            // in any case, if there's nothing, don't display the loadMore button!!
            holder.itemView.loadMoreButton.visibility = View.GONE
            return
        }

        if (holder.itemViewType == viewTypeFooter)
        {
            if (Requestor.instance.isLoadMoreVisible)
                holder.itemView.loadMoreButton.visibility = View.VISIBLE
            else
                holder.itemView.loadMoreButton.visibility = View.GONE
            holder.itemView.loadMoreButton.text = "Load more results"
            holder.itemView.loadMoreButton.setOnClickListener{
                Requestor.instance.loadMore()
            }
            return
        }

        val artist = holder.itemView.findViewById<TextView>(R.id.request_song_artist)
        val title = holder.itemView.findViewById<TextView>(R.id.request_song_title)
        val button = holder.itemView.request_button

        if (dataSet[position].isRequestable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) // for API21+ Material Design makes ripples on the button.
                button.supportBackgroundTintList = colorGreenList
            else // But on API20- no Material Design support, so we add some more color when clicked
                button.supportBackgroundTintList = colorGreenListCompat
            button.isEnabled = true
            button.isClickable = true
            button.setOnClickListener {
                Requestor.instance.request(dataSet[position].id)
            }
        } else {
            button.supportBackgroundTintList = colorRedList
            button.isEnabled = false
            button.isClickable = false
        }

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            button,2, 24, 2, TypedValue.COMPLEX_UNIT_SP)
        artist.text = dataSet[position].artist.value
        title.text = dataSet[position].title.value
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size + 1 // add 1 for the "Load more results" button

    override fun getItemViewType(position: Int): Int {
        return if (position == dataSet.size) viewTypeFooter else viewTypeCell
    }

    /*
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.song_view, parent, false) as ConstraintLayout
    }
    */

    // a filtering function. As naive as it could be, but it should work.
    private val dataSetOrig = ArrayList<Song>()
    init {
        dataSetOrig.addAll(dataSet)
    }

    fun filter(entry: String) {
        var text = entry
        dataSet.clear()
        Log.d(tag, "entering filter")
        if (text.isEmpty()) {
            dataSet.addAll(dataSetOrig)
        } else {
            text = text.toLowerCase(locale = Locale.ROOT)
            for (item in dataSetOrig) {
                Log.d(tag, "$text, ${item.artist.value!!.toLowerCase(locale = Locale.ROOT)}, ${item.title.value!!.toLowerCase(locale = Locale.ROOT)}")
                if (item.artist.value!!.toLowerCase(locale = Locale.ROOT).contains(text) ||
                    item.title.value!!.toLowerCase(locale = Locale.ROOT).contains(text)) {
                    dataSet.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

}

