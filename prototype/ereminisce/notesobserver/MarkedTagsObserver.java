package com.rasalhague.eremenice.ereminisce.notesobserver;

import com.evernote.clients.NoteStoreClient;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.Tag;
import com.evernote.thrift.TException;
import rasalhague.ereminisce.auth.AuthManager;

import java.util.ArrayList;
import java.util.List;

public class MarkedTagsObserver implements Runnable, AuthManager.AuthSuccessListener
{
    private NoteStoreClient noteStore;
    private final String                      TAG_FILTER             = "(?<ER>ER):(?<FirstReminisce>\\d+):(?<Ratio>\\d+(.?\\d+)?)";
    private       List<TagsToAnalyzeListener> tagsToAnalyzeListeners = new ArrayList<TagsToAnalyzeListener>();

    public void startObservation()
    {
        Thread thread = new Thread(this);
        thread.setName(getClass().getName());
//        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run()
    {
        ArrayList<Tag> myTags = new ArrayList<Tag>();

        List<Tag> tags = loadTagList();
        for (Tag tag : tags)
        {
            boolean matches = tag.getName().matches(TAG_FILTER);
            if (matches)
            {
                myTags.add(tag);
            }
        }

        if (myTags.size() > 0)
        {
            notifyTagsToAnalyzeListeners(myTags);
        }
    }

    private List<Tag> loadTagList()
    {
        List<Tag> tagList = null;

        try
        {
            tagList = noteStore.listTags();
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

        return tagList;
    }

    private List<String> extractTagsGUID(List<Tag> tagList)
    {
        ArrayList<String> tagsGUID = new ArrayList<String>();

        for (Tag tag : tagList)
        {
            tagsGUID.add(tag.getGuid());
        }

        return tagsGUID;
    }

    @Override
    public void authSucceed(NoteStoreClient noteStore)
    {
        this.noteStore = noteStore;

        startObservation();
    }

    public void addMarkedNotesFoundListener(TagsToAnalyzeListener listener) { tagsToAnalyzeListeners.add(listener);}

    public void removeMarkedNotesFoundListener(TagsToAnalyzeListener listener)
    {
        tagsToAnalyzeListeners.remove(listener);
    }

    public void notifyTagsToAnalyzeListeners(ArrayList<Tag> tagsToAnalyze)
    {
        for (TagsToAnalyzeListener listener : tagsToAnalyzeListeners)
        {
            listener.tagsToAnalyzeFound(tagsToAnalyze);
        }
    }

}
