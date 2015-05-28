package com.rasalhague.eremenice.ereminisce.auth;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.thrift.TException;

import java.util.ArrayList;
import java.util.List;

public class AuthManager
{
    private final EvernoteService evernoteService;
    private final String          devToken;
    private final EvernoteAuth    evernoteAuth;
    private       NoteStoreClient noteStore;

    public AuthManager(EvernoteService evernoteService, String devToken)
    {
        this.evernoteService = evernoteService;
        this.devToken = devToken;
        this.evernoteAuth = new EvernoteAuth(evernoteService, devToken);
    }

    public void authAsync()
    {
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                ClientFactory factory = new ClientFactory(evernoteAuth);
                try
                {
                    NoteStoreClient noteStore = factory.createNoteStoreClient();

                    notifyAuthSuccessListeners(noteStore);
                }
                catch (EDAMUserException e)
                {
                    e.printStackTrace();
                }
                catch (EDAMSystemException e)
                {
                    e.printStackTrace();
                }
                catch (TException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public NoteStoreClient getNoteStore()
    {
        return noteStore;
    }

    private List<AuthSuccessListener> authSuccessListeners = new ArrayList<AuthSuccessListener>();

    public void addAuthSuccessListener(AuthSuccessListener listener) { authSuccessListeners.add(listener);}

    public void removeAuthSuccessListener(AuthSuccessListener listener) { authSuccessListeners.remove(listener);}

    public void notifyAuthSuccessListeners(NoteStoreClient noteStore)
    {
        for (AuthSuccessListener listener : authSuccessListeners)
        {
            listener.authSucceed(noteStore);
        }
    }

    public interface AuthSuccessListener
    {
        public void authSucceed(NoteStoreClient noteStore);
    }
}
