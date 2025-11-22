package com.mobdeve.s16.rivera.rolen.mc0_fitnesstracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import org.osmdroid.util.GeoPoint

class MyTrackingService : Service(), SensorEventListener {

    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private var startSteps = -1

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("track_channel", "Tracking", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            stopSelf()
            return START_NOT_STICKY
        }

        val notif = NotificationCompat.Builder(this, "track_channel")
            .setContentTitle("Fitness Tracker Running")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
        startForeground(1, notif)

        GlobalData.isTracking = true
        GlobalData.clearData()
        startSteps = -1

        startGPS()
        startPedometer()

        return START_STICKY
    }

    private fun startGPS() {
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        try {
            locationClient.requestLocationUpdates(req, object : LocationCallback() {
                override fun onLocationResult(res: LocationResult) {
                    val loc = res.lastLocation ?: return
                    val newPoint = GeoPoint(loc.latitude, loc.longitude)

                    val route = GlobalData.liveRoute.value ?: mutableListOf()
                    route.add(newPoint)
                    GlobalData.liveRoute.postValue(route)

                    if (route.size > 1) {
                        val prev = route[route.size - 2]
                        val results = FloatArray(1)
                        Location.distanceBetween(prev.latitude, prev.longitude, newPoint.latitude, newPoint.longitude, results)
                        val dist = GlobalData.liveDistance.value ?: 0f
                        GlobalData.liveDistance.postValue(dist + results[0])
                    }
                }
            }, Looper.getMainLooper())
        } catch (e: SecurityException) { }
    }

    private fun startPedometer() {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val raw = event.values[0].toInt()
            if (startSteps == -1) startSteps = raw
            val steps = raw - startSteps
            GlobalData.liveSteps.postValue(steps)
            GlobalData.liveCalories.postValue((steps * 0.04).toInt())
        }
    }

    override fun onAccuracyChanged(s: Sensor?, a: Int) {}

    override fun onDestroy() {
        GlobalData.isTracking = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}