package com.rasalhague.ereminisce.scanner.service;

import com.evernote.clients.NoteStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Tag;
import com.evernote.thrift.TException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rasalhague.ereminisce.Utils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceDataManager
{
    private final static Logger logger             = Logger.getLogger(ServiceDataManager.class);
    private final        String SERVICE_NOTE_TITLE = "ERemenice Service Note";
    private final        String SERVICE_TAG_NAME   = "ERemenice Service Note";
    private final        String CONTENT_FILTER     = "(<en-note>)(.*?)(?<Content>\\{.*\\})?(.*)(<\\/en-note>)";
    private final        Gson   gson               = new GsonBuilder().create();
    private final NoteStoreClient noteStoreClient;
    private long serviceDataLoadRetryDelay = 5000;
    private ServiceData serviceData;

    public ServiceDataManager(NoteStoreClient noteStoreClient)
    {
        this.noteStoreClient = noteStoreClient;
    }

    public ServiceData getServiceData()
    {
        if (serviceData == null)
        {
            serviceData = loadServiceData();
        }
        return serviceData;
    }

    public void updateServiceData()
    {
        String serviceDataJson = gson.toJson(serviceData);

        String nBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        nBody += "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">";
        nBody += "<en-note>" + serviceDataJson + "</en-note>";

        Note note = loadServiceNote();
        note.setContent(nBody);

        try
        {
            noteStoreClient.updateNote(note);
        }
        catch (EDAMUserException | EDAMSystemException | TException | EDAMNotFoundException e)
        {
            logger.info(Utils.getStackTraceString(e));
        }
    }

    private ServiceData loadServiceData()
    {
        Note note = loadServiceNote();

        String content = note.getContent();
        Matcher matcher = Pattern.compile(CONTENT_FILTER, Pattern.DOTALL).matcher(content);
        String filteredContent = "";
        if (matcher.find())
        {
            filteredContent = matcher.group("Content");
        }

        /**
         * Must return felt and recognized ServiceData
         * else data will override in NoteProcessor.addNewNotesToServiceData() with new DateTime()
         * that causing time update for all ER notes
         *
         * So, do recursive call
         * You Shell Not Pass
         */
        ServiceData serviceData;
        try
        {
            serviceData = gson.fromJson(filteredContent, ServiceData.class);

            if (serviceData == null) throw new NullPointerException("serviceData == null");
        }
        catch (Exception e)
        {
            logger.warn(Utils.getStackTraceString(e));
            logger.warn("content: " + content);
            logger.warn("filteredContent: " + filteredContent);
            logger.warn("Starting recursive calling");

            Utils.sleep(serviceDataLoadRetryDelay);

            serviceData = loadServiceData();
        }

        return serviceData;
    }

    private Note loadServiceNote()
    {
        Note serviceNote;
        try
        {
            String serviceTagGUID = loadServiceTagGUID();
            serviceNote = loadServiceNote(serviceTagGUID);
        }
        catch (TException | EDAMUserException | EDAMSystemException | EDAMNotFoundException e)
        {
            logger.info(Utils.getStackTraceString(e));
            serviceNote = loadServiceNote();
        }

        return serviceNote;
    }

    private Tag createServiceTag() throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException
    {
        Tag tagToCreate = new Tag();
        tagToCreate.setName(SERVICE_TAG_NAME);

        return noteStoreClient.createTag(tagToCreate);
    }

    private Note createServiceNote() throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException
    {
        ArrayList<String> serviceNoteTagGUID = new ArrayList<>();
        serviceNoteTagGUID.add(loadServiceTagGUID());

        ServiceData serviceData = new ServiceData();
        serviceData.getNotesUpdateTime().put("ServiceNoteCreateDate", new DateTime().getMillis());
        String serviceDataJson = gson.toJson(serviceData);

        String nBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        nBody += "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">";
        nBody += "<en-note>" + serviceDataJson + "</en-note>";

        Note serviceNote = new Note();
        serviceNote.setTagGuids(serviceNoteTagGUID);
        serviceNote.setTitle(SERVICE_NOTE_TITLE);
        serviceNote.setContent(nBody);

        Note note = noteStoreClient.createNote(serviceNote);

        return noteStoreClient.getNote(note.getGuid(), true, true, true, true);
    }

    private String loadServiceTagGUID() throws TException, EDAMUserException, EDAMSystemException, EDAMNotFoundException
    {
        List<String> serviceTagGUIDs = new ArrayList<>();

        //        noteStoreClient.listTags().forEach(tag -> {
        //            if (tag.getName().equals(SERVICE_NOTE_NAME))
        //            {
        //                serviceTagGUID.add(tag.getGuid());
        //            }
        //        });

        noteStoreClient.listTags()
                       .stream()
                       .filter(tag -> tag.getName().equals(SERVICE_TAG_NAME))
                       .forEach(tag -> serviceTagGUIDs.add(tag.getGuid()));

        if (serviceTagGUIDs.size() == 0)
        {
            return createServiceTag().getGuid();
        }

        if (serviceTagGUIDs.size() > 1)
        {
            logger.info("WTF: More then one Service tag. Returning first. serviceTagGUIDs: " + serviceTagGUIDs);
        }

        return serviceTagGUIDs.get(0);
    }

    private Note loadServiceNote(String serviceTagGUID) throws
                                                        EDAMUserException,
                                                        EDAMSystemException,
                                                        TException,
                                                        EDAMNotFoundException
    {
        List<String> serviceTagGUIDList = new ArrayList<>();
        serviceTagGUIDList.add(serviceTagGUID);

        NoteFilter noteFilter = new NoteFilter();
        noteFilter.setTagGuids(serviceTagGUIDList);

        NotesMetadataResultSpec notesMetadataResultSpec = new NotesMetadataResultSpec();
        notesMetadataResultSpec.setIncludeCreated(true);
        notesMetadataResultSpec.setIncludeUpdated(true);
        notesMetadataResultSpec.setIncludeAttributes(true);
        notesMetadataResultSpec.setIncludeTitle(true);
        notesMetadataResultSpec.setIncludeTagGuids(true);
        notesMetadataResultSpec.setIncludeContentLength(true);

        NotesMetadataList notesMetadataList = noteStoreClient.findNotesMetadata(noteFilter,
                                                                                0,
                                                                                Short.MAX_VALUE,
                                                                                notesMetadataResultSpec);

        if (notesMetadataList.getTotalNotes() == 0)
        {
            return createServiceNote();
        }

        if (notesMetadataList.getTotalNotes() > 1)
        {
            logger.info("More then one Service note. Returning first. notesMetadataList: " + notesMetadataList);
        }

        return noteStoreClient.getNote(notesMetadataList.getNotes().get(0).getGuid(), true, true, true, true);
    }
}
