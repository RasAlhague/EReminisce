package com.rasalhague.ereminisce.scanner;

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
import com.rasalhague.ereminisce.Utils;
import com.rasalhague.ereminisce.properties.Properties;
import com.rasalhague.ereminisce.properties.PropertiesNames;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Scanner implements ScannerTimer.TimerTick
{
    private final static Logger            logger                   = Logger.getLogger(Scanner.class);
    private final        String            TAG_IDENTIFICATION_REGEX = "(?<ER>ER) (?<FirstReminisce>\\d+)";
    private long retryDelay = 300000; // 5min
    private              ScannerObservable scannerObservable        = new ScannerObservable();
    private NoteStoreClient noteStoreClient;
    private List<Tag>       filteredTags;
    private ScannerTimer    scannerTimer;

    public Scanner(NoteStoreClient noteStoreClient, Properties properties)
    {
        this.noteStoreClient = noteStoreClient;
        this.scannerTimer = new ScannerTimer(this);

        setUpFieldsFromProperties(properties);
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
            if (propertiesMap.containsKey(PropertiesNames.SCAN_PERIOD))
            {
                long parseLong = Long.parseLong(propertiesMap.get(PropertiesNames.SCAN_PERIOD));
                setScannerPeriod(parseLong);
                logger.info(PropertiesNames.SCAN_PERIOD + " set up to " + parseLong);
            }
        }
        catch (Exception e)
        {
            logger.info(Utils.getStackTraceString(e));
        }
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
                Thread.sleep(retryDelay);
                notes = loadNotesByTags(tags);
            }
            catch (InterruptedException e1)
            {
                logger.info(Utils.getStackTraceString(e1));
            }
        }

        return notes;
    }

}
