package com.rasalhague.eremenice.scanner.service;

import java.util.HashMap;

public class ServiceData
{
    private HashMap<String, Long> notesUpdateTime = new HashMap<>();

    public HashMap<String, Long> getNotesUpdateTime()
    {
        return notesUpdateTime;
    }

    @Override
    public String toString()
    {
        return "ServiceData{" +
                "notesUpdateTime=" + notesUpdateTime +
                '}';
    }
}
