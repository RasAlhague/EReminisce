package com.rasalhague.ereminisce.scanner;

import java.util.TimerTask;

public class ScannerTimer
{
    private long            scannerStartDelay = 0;
    private long            scannerPeriod     = 5000;
    private java.util.Timer timer             = new java.util.Timer("Scanner timer");
    private TimerSchedule   timerSchedule     = new TimerSchedule();
    private TimerTick timerTickListener;

    public ScannerTimer(TimerTick timerTickListener)
    {
        this.timerTickListener = timerTickListener;
    }

    public void start()
    {
        startSchedule();
    }

    public void stop()
    {
        timer.cancel();
    }

    public long getScannerPeriod()
    {
        return scannerPeriod;
    }

    public void setScannerPeriod(long scannerPeriod)
    {
        this.scannerPeriod = scannerPeriod;
    }

    private void startSchedule()
    {
        timer.schedule(timerSchedule, scannerStartDelay, scannerPeriod);
    }

    interface TimerTick
    {
        void onTimerTick();
    }

    private class TimerSchedule extends TimerTask
    {
        @Override
        public void run()
        {
            timerTickListener.onTimerTick();
        }
    }
}
