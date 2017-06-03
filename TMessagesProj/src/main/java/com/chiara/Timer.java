package com.chiara;

import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Chiara on 09/04/17.
 */

public class Timer {
    long startTime;
    long endTime;
    long startPause;
    long endPause;
    long pausedTime;
    long totalTime;
    long totalTimeWithoutPauses;

    public Timer(){
        startTime = 0;
        endTime = 0;
        pausedTime = 0;
        totalTime = 0;
    }

    public long getStartTime(){
        startTime = SystemClock.uptimeMillis();
        Log.i("starttime",Long.toString(startTime));
        return startTime;
    }

    public long getStartPause(){
        startPause = SystemClock.uptimeMillis();
        return startPause;
    }

    public void onEndPause(long startPause){
        endPause = SystemClock.uptimeMillis();
        long pauseLength = endPause - startPause;
        pausedTime += pauseLength;
    }

    public long getEndTime(){
        Log.i("endtime",Long.toString(endTime));
        endTime = SystemClock.uptimeMillis();
        return endTime;
    }

    public long getTotalTime(){
        totalTime = endTime - startTime - pausedTime;
        return totalTime;
    }

    public long getTotalTimeWithoutPauses(){
        totalTimeWithoutPauses = endTime - startTime;
        return totalTimeWithoutPauses;
    }
}
