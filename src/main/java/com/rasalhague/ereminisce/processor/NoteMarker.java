package com.rasalhague.ereminisce.processor;

import com.evernote.clients.NoteStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Tag;
import com.evernote.thrift.TException;
import com.rasalhague.ereminisce.Utils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class NoteMarker
{
    private final static Logger logger = Logger.getLogger(NoteMarker.class);
    private final NoteStoreClient noteStoreClient;
    private final String MARK_TAG_NAME = "EReminisce";
    private long retryDelay = 300000; // 5min
    private String markTagGUID;

    public NoteMarker(NoteStoreClient noteStoreClient)
    {
        this.noteStoreClient = noteStoreClient;
    }

    public boolean mark(List<NoteMetadata> noteMetadatas)
    {
        try
        {
            for (NoteMetadata noteMetadata : noteMetadatas)
            {
                List<String> markTagGUIDs = new ArrayList<>();
                markTagGUIDs.add(loadMarkTagGUID());
                markTagGUIDs.addAll(noteMetadata.getTagGuids());

                Note note = noteStoreClient.getNote(noteMetadata.getGuid(), false, false, false, false);
                note.setTagGuids(markTagGUIDs);

                noteStoreClient.updateNote(note);
            }

            return true;
        }
        catch (EDAMUserException | EDAMSystemException | EDAMNotFoundException | TException e)
        {
            logger.warn("Note have not marked");
            logger.warn(Utils.getStackTraceString(e));
            logger.warn("Waiting " + retryDelay + " ms for retry");

            Utils.sleep(retryDelay);

            return mark(noteMetadatas);
        }
    }

    private String loadMarkTagGUID() throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException
    {
        if (markTagGUID == null)
        {
            Tag tag = new Tag();
            tag.setName(MARK_TAG_NAME);

            List<Tag> tagList = noteStoreClient.listTags();
            for (Tag tag1 : tagList)
            {
                if (tag1.getName().equals(MARK_TAG_NAME))
                {
                    markTagGUID = tag1.getGuid();
                    return markTagGUID;
                }
            }

            markTagGUID = noteStoreClient.createTag(tag).getGuid();
        }

        return markTagGUID;
    }
}
