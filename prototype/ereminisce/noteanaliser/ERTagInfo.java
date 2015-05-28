package com.rasalhague.eremenice.ereminisce.noteanaliser;

public class ERTagInfo
{
    private int   firstReminisce;
    private float ratio;

    public ERTagInfo(int firstReminisce, float ratio)
    {
        this.firstReminisce = firstReminisce;
        this.ratio = ratio;
    }

    public int getFirstReminisce()
    {
        return firstReminisce;
    }

    public float getRatio()
    {
        return ratio;
    }
}
