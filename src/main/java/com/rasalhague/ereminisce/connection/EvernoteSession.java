package com.rasalhague.ereminisce.connection;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.thrift.TException;
import com.rasalhague.ereminisce.Utils;
import com.rasalhague.ereminisce.properties.Properties;
import com.rasalhague.ereminisce.properties.PropertiesNames;
import org.apache.log4j.Logger;

import java.util.HashMap;

public class EvernoteSession
{
    private final static Logger          logger          = Logger.getLogger(EvernoteSession.class);
    private long retryDelay = 300000; // 5min
    private              String          devToken        = "S=s1:U=90da5:E=154bd566f51:C=14d65a54318:P=1cd:A=en-devtoken:V=2:H=6255af9a78934f41a9274f698651f05e";
    private              EvernoteService evernoteService = EvernoteService.SANDBOX;
    private EvernoteAuth evernoteAuth;

    public EvernoteSession(Properties properties)
    {
        evernoteAuth = new EvernoteAuth(evernoteService, devToken);

        setUpFieldsFromProperties(properties);
    }

    public EvernoteSession(Properties properties, EvernoteService evernoteService, String devToken)
    {
        this.evernoteService = evernoteService;
        this.evernoteAuth = new EvernoteAuth(evernoteService, devToken);
        this.devToken = devToken;

        setUpFieldsFromProperties(properties);
    }

    public NoteStoreClient open()
    {
        ClientFactory factory = new ClientFactory(evernoteAuth);
        try
        {
            return factory.createNoteStoreClient();
        }
        catch (EDAMUserException | EDAMSystemException | TException e)
        {
            logger.info(Utils.getStackTraceString(e));

            try
            {
                Thread.sleep(retryDelay);
                return open();
            }
            catch (InterruptedException e1)
            {
                logger.info(Utils.getStackTraceString(e1));
            }
        }

        //in normal situation program will never rich this point
        return null;
    }

    private void setUpFieldsFromProperties(Properties properties)
    {
        try
        {
            HashMap<String, String> propertiesMap = properties.getPropertiesMap();

            if (propertiesMap.containsKey(PropertiesNames.ON_EXCEPTION_RETRY_DELAY))
            {
                long parseLong = Long.parseLong(propertiesMap.get(PropertiesNames.ON_EXCEPTION_RETRY_DELAY));
                retryDelay = parseLong;
                logger.info(PropertiesNames.ON_EXCEPTION_RETRY_DELAY + " set up to " + parseLong);
            }
        }
        catch (Exception e)
        {
            logger.info(Utils.getStackTraceString(e));
        }
    }
}
