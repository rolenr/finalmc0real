package com.mobdeve.s16.rivera.rolen.mc0_fitnesstracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

class TrackingActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private var line: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // INIT OSM CONFIG
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_tracking)

        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)
        map.controller.setZoom(18.0)
        map.controller.setCenter(GeoPoint(14.5995, 120.9842)) // Default Manila

        btnStart = findViewById(R.id.btnStartWorkout)
        btnStop = findViewById(R.id.btnStopWorkout)
        val tvDist = findViewById<TextView>(R.id.tvDistance)
        val tvSteps = findViewById<TextView>(R.id.tvSteps)
        val tvCals = findViewById<TextView>(R.id.tvCalories)

        // OBSERVE DATA
        GlobalData.liveDistance.observe(this) { tvDist.text = String.format("%.1f m", it) }
        GlobalData.liveSteps.observe(this) { tvSteps.text = "$it" }
        GlobalData.liveCalories.observe(this) { tvCals.text = "$it" }

        GlobalData.liveRoute.observe(this) { points ->
            if (points.isNotEmpty()) {
                if (line != null) map.overlays.remove(line)
                line = Polyline()
                line?.setPoints(points)
                line?.color = Color.BLUE
                line?.width = 10f
                map.overlays.add(line)
                map.invalidate()
                map.controller.animateTo(points.last())
            }
        }

        btnStart.setOnClickListener {
            if (checkPerms()) {
                startService(Intent(this, MyTrackingService::class.java))
                updateUI(true)
            } else {
                reqPerms()
            }
        }

        btnStop.setOnClickListener {
            val intent = Intent(this, MyTrackingService::class.java)
            intent.action = "STOP_SERVICE"
            startService(intent)
            updateUI(false)

            val db = DatabaseHelper(this)
            val saved = db.addWorkout(
                System.currentTimeMillis(),
                GlobalData.liveDistance.value ?: 0f,
                GlobalData.liveSteps.value ?: 0,
                GlobalData.liveCalories.value ?: 0
            )
            Toast.makeText(this, if (saved) "Saved!" else "Error", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.fabHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        if (GlobalData.isTracking) updateUI(true)
    }

    private fun updateUI(tracking: Boolean) {
        btnStart.visibility = if (tracking) View.GONE else View.VISIBLE
        btnStop.visibility = if (tracking) View.VISIBLE else View.GONE
    }

    private fun checkPerms() = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    private fun reqPerms() = ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION), 1)

    override fun onResume() { super.onResume(); map.onResume() }
    override fun onPause() { super.onPause(); map.onPause() }
}