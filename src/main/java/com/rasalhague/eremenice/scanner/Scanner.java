package com.rasalhague.eremenice.scanner;

import com.evernote.clients.NoteStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.Tag;
import com.evernote.thrift.TException;
import com.rasalhague.eremenice.Utils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Scanner implements ScannerTimer.TimerTick
{
    private final static Logger            logger                   = Logger.getLogger(Scanner.class);
    private final        String            TAG_IDENTIFICATION_REGEX = "(?<ER>ER) (?<FirstReminisce>\\d+)";
    private final        long              RETRY_DELAY              = 5000;
    private              ScannerObservable scannerObservable        = new ScannerObservable();
    private NoteStoreClient noteStoreClient;
    private List<Tag>       filteredTags;
    private ScannerTimer    scannerTimer;

    public Scanner(NoteStoreClient noteStoreClient)
    {
        this.noteStoreClient = noteStoreClient;
        this.scannerTimer = new ScannerTimer(this);
    }

    public ScannerObservable getScannerObservable()
    {
        return scannerObservable;
    }

    public void setScannerPeriod(long scannerPeriod)
    {
        scannerTimer.setScannerPeriod(scannerPeriod);
    }

    public void startSchedule()
    {
        scannerTimer.start();
    }

    public void stopSchedule()
    {
        scannerTimer.stop();
    }

    public List<NoteMetadata> loadTaggedNotes()
    {
        List<Tag> tagList = loadTagList();
        filteredTags = filterTags(tagList);

        return loadNotesByTags(filteredTags);
    }

    @Override
    public void onTimerTick()
    {
        List<NoteMetadata> noteMetadatas = loadTaggedNotes();
        scannerObservable.notifyTaggedNotesLoadedListeners(noteMetadatas, filteredTags);
    }

    private List<Tag> loadTagList()
    {
        List<Tag> tagList = new ArrayList<>();

        try
        {
            tagList = noteStoreClient.listTags();
        }
        catch (EDAMUserException | EDAMSystemException | TException e)
        {
            logger.info(Utils.getStackTraceString(e));
        }

        return tagList;
    }

    private List<Tag> filterTags(List<Tag> tags)
    {
        return tags.stream()
                   .filter(tag -> tag.getName().matches(TAG_IDENTIFICATION_REGEX))
                   .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> extractTagsGUID(List<Tag> tagList)
    {
        return tagList.stream().map(Tag::getGuid).collect(Collectors.toList());
    }

    private List<NoteMetadata> loadNotesByTags(List<Tag> tags)
    {
        NoteFilter noteFilter = new NoteFilter();

        NotesMetadataResultSpec notesMetadataResultSpec = new NotesMetadataResultSpec();
        notesMetadataResultSpec.setIncludeTagGuids(true);
        notesMetadataResultSpec.setIncludeTitle(true);
        notesMetadataResultSpec.setIncludeAttributes(true);
        notesMetadataResultSpec.setIncludeUpdated(true);
        notesMetadataResultSpec.setIncludeCreated(true);

        NotesMetadataList notesMetadata;
        List<NoteMetadata> notes = new ArrayList<>();
        try
        {
            List<String> tagsGUID = extractTagsGUID(tags);
            for (int i = 0; i < tagsGUID.size(); i++)
            {
                noteFilter.setTagGuids(tagsGUID.subList(i, i + 1));
                notesMetadata = noteStoreClient.findNotesMetadata(noteFilter,
                                                                  0,
                                                                  Short.MAX_VALUE,
                                                                  notesMetadataResultSpec);
                notes.addAll(notesMetadata.getNotes());
            }
        }
        catch (EDAMUserException | EDAMSystemException | TException | EDAMNotFoundException e)
        {
            logger.info(Utils.getStackTraceString(e));

            try
            {
                Thread.sleep(RETRY_DELAY);
                loadNotesByTags(tags);
            }
            catch (InterruptedException e1)
            {
                logger.info(Utils.getStackTraceString(e1));
            }
        }

        return notes;
    }

}
