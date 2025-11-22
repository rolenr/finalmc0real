package com.mobdeve.s16.rivera.rolen.mc0_fitnesstracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Simple data container
data class WorkoutModel(
    val id: Int,
    val date: Long,
    val distance: Float,
    val steps: Int,
    val calories: Int
)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "fitness_osm.db", null, 1) {

    // 1. CREATE THE TABLE
    // This runs only once when the user first installs the app
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE workouts (id INTEGER PRIMARY KEY AUTOINCREMENT, date LONG, dist FLOAT, steps INTEGER, cals INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS workouts")
        onCreate(db)
    }

    fun addWorkout(date: Long, dist: Float, steps: Int, cals: Int): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("date", date)
        cv.put("dist", dist)
        cv.put("steps", steps)
        cv.put("cals", cals)
        val result = db.insert("workouts", null, cv)
        db.close()
        return result != -1L
    }

    fun getAllWorkouts(): List<WorkoutModel> {
        val list = ArrayList<WorkoutModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM workouts ORDER BY date DESC", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(WorkoutModel(
                    cursor.getInt(0),
                    cursor.getLong(1),
                    cursor.getFloat(2),
                    cursor.getInt(3),
                    cursor.getInt(4)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }
}