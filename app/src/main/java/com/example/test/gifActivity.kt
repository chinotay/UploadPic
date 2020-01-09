package com.example.test

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_gif.view.*
import kotlinx.android.synthetic.main.list_layout.*
import kotlinx.android.synthetic.main.list_layout.view.*
import java.util.*


class gifActivity : AppCompatActivity() {


    lateinit var mRecyclerView: RecyclerView
    lateinit var mDatabase: DatabaseReference
    lateinit var aContext: Context
    // private var nname: TextView? = null
    //private var iimage: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif)
        mDatabase = FirebaseDatabase.getInstance().getReference("workout")
        //    .child(intent.getStringExtra("id")).child("workoutDetail")
        mRecyclerView = findViewById(R.id.recyclerView)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = GridLayoutManager(this, 2)
        logRecyclerView()
    }

    private fun logRecyclerView() {

        var FirebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<workout, GifViewHolder>(

            workout::class.java,
            R.layout.list_layout,
            GifViewHolder::class.java,
            mDatabase
        ) {
            override fun populateViewHolder(
                viewHolder: GifViewHolder?,
                model: workout?,
                position: Int
            ) {
                viewHolder?.itemView?.exerciseDesc?.text = model?.name      //show name
                Glide.with(viewHolder?.itemView?.context)               // show gif
                    .load(model?.gifUrl)
                    .into(viewHolder?.itemView?.exerciseImg)
                //Glide.with(aContext)
                //  .load(model?.gifUrl)
                //.into(exerciseImg)
            }

        }
        mRecyclerView.adapter = FirebaseRecyclerAdapter

    }

    class GifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {

            itemView.setOnClickListener {

                val intent = Intent(itemView.context, Timer::class.java)
                intent.putExtra("link", itemView.link.text)
                itemView.context.startActivity(intent)
            }
        }
    }
}
