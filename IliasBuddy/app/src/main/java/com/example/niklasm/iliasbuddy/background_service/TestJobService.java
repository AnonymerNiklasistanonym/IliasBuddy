package com.example.niklasm.iliasbuddy.background_service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

import com.example.niklasm.iliasbuddy.BackgroundIntentService;

public class TestJobService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.i("TestJobService", "onStartJob");
        // run background service
        getApplicationContext()
                .startService(new Intent(getApplicationContext(), BackgroundIntentService.class));
        // reschedule the job
        Util.scheduleJob(getApplicationContext());
        return true;
    }

    @Override
    public boolean onStopJob(final JobParameters params) {
        Log.i("TestJobService", "onStopJob");
        return true;
    }
}
