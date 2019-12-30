package fr.forum_thalie.tsumugi.ui.news

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LevelListDrawable
import android.os.AsyncTask
import android.text.Html.ImageGetter
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import fr.forum_thalie.tsumugi.R
import fr.forum_thalie.tsumugi.newsDisplayDatePattern
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

// Using solution found here: https://stackoverflow.com/questions/3758535/display-images-on-android-using-textview-and-html-imagegetter-asynchronously
// but without Picasso (just fetching image by myself.)
class ImageGetterAsyncTask(
    private val context: Context,
    private val source: String,
    private val levelListDrawable: LevelListDrawable
) :
    AsyncTask<TextView?, Void?, Bitmap?>() {
    private var t: TextView? = null
    override fun doInBackground(vararg params: TextView?): Bitmap? {
        t = params[0]
        return try {
            ////[REMOVE LOG CALLS]Log.d(LOG_CAT, "Downloading the image from: $source")
            var k: InputStream? = null
            var pic: Bitmap? = null
            try {
                k = URL(source).content as InputStream
                val options = BitmapFactory.Options()
                options.inSampleSize = 1
                // Putting 2 makes 1/2 of origin image size from width and height.
                // it alleviates the memory and CPU too for weak devices.
                pic = BitmapFactory.decodeStream(k, null, options)
                k.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                k?.close()
            }
            pic
        } catch (e: Exception) {
            null
        }
    }

    override fun onPostExecute(bitmap: Bitmap?) {
        try {
            val d: Drawable = BitmapDrawable(context.resources, bitmap)
            val size = Point()
            (context as Activity).windowManager.defaultDisplay.getSize(size)
            // Lets calculate the ratio according to the screen width in px
            val multiplier: Double = (size.x).toDouble() / (bitmap!!.width.toDouble())
            levelListDrawable.addLevel(1, 1, d)
            // Set bounds width  and height according to the bitmap resized size
            levelListDrawable.setBounds(
                0,
                0,
                (bitmap.width.toDouble() * multiplier).toInt(),
                (bitmap.height.toDouble() * multiplier).toInt()
            )
            levelListDrawable.level = 1
            t!!.text = t!!.text // invalidate() doesn't work correctly...
        } catch (e: Exception) { /* Like a null bitmap, etc. */
        }
    }

}

class NewsAdapter(private val dataSet: ArrayList<News>, private val c: Context, private val vm: NewsViewModel
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

        val titleLink = "<a href=\"${dataSet[position].link}\">${dataSet[position].title}</>"
        title.text = HtmlCompat.fromHtml(titleLink, HtmlCompat.FROM_HTML_MODE_LEGACY)
        title.movementMethod = LinkMovementMethod.getInstance()

        header.text = HtmlCompat.fromHtml(dataSet[position].header, HtmlCompat.FROM_HTML_MODE_LEGACY).replace(Regex("\n"), " ")
        author.text = "| ${dataSet[position].author}"
        val sdf = SimpleDateFormat(newsDisplayDatePattern, Locale.getDefault())
        date.text = sdf.format(dataSet[position].date)
        TextViewCompat.setAutoSizeTextTypeWithDefaults(author, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)

        val spanned = // the trick is to avoid loading images when the adapter is called with preloading.
            if (vm.isPreLoadingNews) {
                HtmlCompat.fromHtml(
                    dataSet[position].text,
                    HtmlCompat.FROM_HTML_MODE_LEGACY)
            } else {
                HtmlCompat.fromHtml(
                    dataSet[position].text,
                    HtmlCompat.FROM_HTML_MODE_LEGACY,
                    ImageGetter { source ->
                        val d = LevelListDrawable()
                        /*
                        val empty: Drawable? = ContextCompat.getDrawable(c, R.drawable.exo_icon_play)
                        d.addLevel(0, 0, empty!!)
                        d.setBounds(0, 0, empty.intrinsicWidth, empty.intrinsicHeight)
                         */
                        ImageGetterAsyncTask(c, source, d).execute(text)
                        d
                    }, null
                )
            }



        text.text = spanned
        text.movementMethod = LinkMovementMethod.getInstance()
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

