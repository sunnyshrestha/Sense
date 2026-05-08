package com.sunny.sense

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

class DataBackupManager(private val firebaseAuthManager: FirebaseAuthManager) {

    private val database = FirebaseDatabase.getInstance().reference

    suspend fun backupDailyData(date: Instant, steps: Long, calories: Double) {
        val user = firebaseAuthManager.getCurrentUser()
        if (user != null) {
            withContext(Dispatchers.IO) {
                val dateString = date.toString().substringBefore("T")
                val data = mapOf(
                    "steps" to steps,
                    "calories" to calories
                )

                database.child("users")
                    .child(user.uid)
                    .child("daily_stats")
                    .child(dateString)
                    .setValue(data)
            }
        }
    }
}
