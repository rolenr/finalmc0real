package com.mobdeve.s16.rivera.rolen.mc0_fitnesstracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_activity)

        val rv = findViewById<RecyclerView>(R.id.rvHistory)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = MyAdapter(DatabaseHelper(this).getAllWorkouts())
    }
}

class MyAdapter(private val list: List<WorkoutModel>) : RecyclerView.Adapter<MyAdapter.Holder>() {
    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val date: TextView = v.findViewById(R.id.tvItemDate)
        val dist: TextView = v.findViewById(R.id.tvItemDistance)
        val steps: TextView = v.findViewById(R.id.tvItemSteps)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_workout_session, parent, false))
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = list[position]
        holder.date.text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(item.date))
        holder.dist.text = "${item.distance} m"
        holder.steps.text = "${item.steps} steps"
    }
    override fun getItemCount() = list.size
}