package com.rasalhague.ereminisce.scanner.service;

import java.util.HashMap;

public class ServiceData
{
    private HashMap<String, Long> notesUpdateTime = new HashMap<>();
    private HashMap<String, Long> notesRipeDays   = new HashMap<>();

    public HashMap<String, Long> getNotesUpdateTime()
    {
        return notesUpdateTime;
    }

    public HashMap<String, Long> getNotesRipeDays()
    {
        return notesRipeDays;
    }

    @Override
    public String toString()
    {
        return "ServiceData{" +
                "notesUpdateTime=" + notesUpdateTime +
                ", notesRipeDays=" + notesRipeDays +
                '}';
    }
}
