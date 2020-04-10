package com.chriskevin.workmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.work.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val workManager = WorkManager.getInstance(applicationContext)

        one_time.setOnClickListener {
            val oneTimeWorkRequest = OneTimeWorkRequestBuilder<Worker>()
                .build()
            workManager.enqueue(oneTimeWorkRequest)
        }

        periodic.setOnClickListener {
            val periodicWorkRequest = PeriodicWorkRequestBuilder<Worker>(15, TimeUnit.MINUTES)
                .setInitialDelay(5, TimeUnit.SECONDS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
        }

        cancel_periodic.setOnClickListener {
            workManager.cancelUniqueWork(WORK_NAME)
        }

        workManager.getWorkInfosForUniqueWorkLiveData(WORK_NAME)
            .observe(this, Observer { listOfWorkInfo ->
                if (listOfWorkInfo.isNullOrEmpty()) {
                    return@Observer
                }

                val workInfo = listOfWorkInfo[0]

                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED -> {
                        cancel_periodic.isEnabled = true
                        periodic.isEnabled = false
                        showSnack("ENQUEUED")
                    }
                    WorkInfo.State.CANCELLED -> {
                        cancel_periodic.isEnabled = false
                        periodic.isEnabled = true
                        showSnack("CANCELLED")
                    }
                    else -> {
                    }
                }
            })
    }

    private fun showSnack(text: String) {
        Snackbar.make(coordinator, text, Snackbar.LENGTH_SHORT)
            .show()
    }
}