package com.rasalhague.ereminisce.processor;

public class TagInfo
{
    private final String guid;
    private final float  delayK;

    public TagInfo(String guid, float delayK)
    {
        this.guid = guid;
        this.delayK = delayK;
    }

    public String getGuid()
    {
        return guid;
    }

    public float getDelayK()
    {
        return delayK;
    }
}
