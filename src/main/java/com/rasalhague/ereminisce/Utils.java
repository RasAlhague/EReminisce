package com.rasalhague.ereminisce;

import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils
{
    private final static Logger logger = Logger.getLogger(Utils.class);

    public static String getStackTraceString(Exception e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        return sw.toString();
    }

    public static void sleep(long timeInMs)
    {
        try
        {
            Thread.sleep(timeInMs);
        }
        catch (InterruptedException e)
        {
            logger.info(Utils.getStackTraceString(e));
        }
    }
}
