package com.rasalhague.eremenice.connection;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.thrift.TException;
import com.rasalhague.eremenice.Utils;
import org.apache.log4j.Logger;

public class EvernoteSession
{
    private final static Logger          logger          = Logger.getLogger(EvernoteSession.class);
    private final        String          DEV_TOKEN       = "S=s1:U=90da5:E=154bd566f51:C=14d65a54318:P=1cd:A=en-devtoken:V=2:H=6255af9a78934f41a9274f698651f05e";
    private final        long            RETRY_DELAY     = 5000;
    private              EvernoteService evernoteService = EvernoteService.SANDBOX;
    private EvernoteAuth evernoteAuth;

    public EvernoteSession()
    {
        evernoteAuth = new EvernoteAuth(evernoteService, DEV_TOKEN);
    }

    public EvernoteSession(EvernoteService evernoteService)
    {
        this.evernoteService = evernoteService;
        evernoteAuth = new EvernoteAuth(evernoteService, DEV_TOKEN);
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
                Thread.sleep(RETRY_DELAY);
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
}
