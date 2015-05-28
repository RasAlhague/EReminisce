package com.rasalhague.eremenice.ereminisce.noteanaliser;

import java.util.ArrayList;

public class RepeatRowCalculator
{
    private int maxDelay = 30;

    public ArrayList<Integer> calculateRow(ERTagInfo erTagInfo)
    {
        ArrayList<Integer> row = new ArrayList<Integer>();

        if (erTagInfo.getFirstReminisce() <= 0 || erTagInfo.getRatio() <= 0) return row;

        int step = 0;
        while (step < maxDelay)
        {
            if (step == 0)
            {
                step = erTagInfo.getFirstReminisce();
            }
            else
            {
                if (erTagInfo.getRatio() == 1)
                {
                    step = step + erTagInfo.getFirstReminisce();
                }
                else
                {
                    step = (int) (step * erTagInfo.getRatio());
                }
            }

            row.add(step);
        }

        return row;
    }
}
