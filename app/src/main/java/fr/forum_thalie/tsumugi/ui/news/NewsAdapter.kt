package fr.forum_thalie.tsumugi.ui.news

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.TextView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import fr.forum_thalie.tsumugi.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NewsAdapter(private val dataSet: ArrayList<News>, private val c: Context
    /*,
    context: Context,
    resource: Int,
    objects: Array<out Song>*/
) : RecyclerView.Adapter<NewsAdapter.MyViewHolder>() /*ArrayAdapter<Song>(context, resource, objects)*/ {


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(view: ConstraintLayout) : RecyclerView.ViewHolder(view)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.news_view, parent, false) as ConstraintLayout
        // set the view's size, margins, paddings and layout parameters
        //...
        return MyViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val title = holder.itemView.findViewById<TextView>(R.id.news_title)
        val text = holder.itemView.findViewById<TextView>(R.id.news_text)
        val author = holder.itemView.findViewById<TextView>(R.id.news_author)
        val header = holder.itemView.findViewById<TextView>(R.id.news_header)
        val date = holder.itemView.findViewById<TextView>(R.id.news_date)
        title.text = dataSet[position].title
        title.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(dataSet[position].link)
            c.startActivity(i)
        }
        text.text = HtmlCompat.fromHtml(dataSet[position].text, HtmlCompat.FROM_HTML_MODE_LEGACY)
        header.text = HtmlCompat.fromHtml(dataSet[position].header, HtmlCompat.FROM_HTML_MODE_LEGACY).replace(Regex("\n"), " ")
        author.text = "| ${dataSet[position].author}"
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        date.text = sdf.format(dataSet[position].date)
        TextViewCompat.setAutoSizeTextTypeWithDefaults(author, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
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

