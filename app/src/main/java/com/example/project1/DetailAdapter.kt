package com.example.project1

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView

class DetailAdapter(val details: List<Detail>, context: Activity) : RecyclerView.Adapter<DetailAdapter.ViewHolder>() {

    private var ctx: Context = context

    // the adapter needs to render a new row and needs to know what xml file to use
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //layout inflation (read and parse XML file and return a reference to the root layout)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_detail, parent, false)
        return ViewHolder(view)
    }

    // the adapter has a row that's ready to be rendered and needs the content filled in
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentDetail = details[position]
        holder.name.setText(currentDetail.name)

        if (currentDetail.price.isNullOrEmpty()){
            holder.price.setText(R.string.no_price)
        } else {
            holder.price.setText(currentDetail.price)
        }

        holder.address.setText(currentDetail.address)

        if (!currentDetail.phone.isNullOrEmpty()){
            holder.call.setOnClickListener {
                //            val intent: Intent = Intent(Intent.ACTION_DIAL) //go from here to MapActivity class
//            intent.setData(currentDetail.phone.toUri())
//            ctx.startActivity(intent) //execute intent


                val callIntent: Intent = Uri.parse(currentDetail.phone).let { number ->
                    Intent(Intent.ACTION_DIAL, number)
                }
                ctx.startActivity(callIntent)
            }
        } else {
            holder.call.isClickable = false
        }

        if (!currentDetail.url.isNullOrEmpty()){
            holder.openBrowser.setOnClickListener {
                val webIntent: Intent = Uri.parse(currentDetail.url).let { webpage ->
                    Intent(Intent.ACTION_VIEW, webpage)
                }
                ctx.startActivity(webIntent)
            }
        } else {
            holder.openBrowser.isClickable = false
            holder.openBrowser.isEnabled = false
        }

        holder.ratingBar.rating = currentDetail.rating.toFloat()
    }

    // return # of rows you expect your list to have
    override fun getItemCount(): Int {
        return details.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val price: TextView = itemView.findViewById(R.id.price)
        val address: TextView = itemView.findViewById(R.id.address)
        val call: ImageButton = itemView.findViewById(R.id.call)
        val openBrowser: ImageButton = itemView.findViewById(R.id.openBrowser)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)

    }
}