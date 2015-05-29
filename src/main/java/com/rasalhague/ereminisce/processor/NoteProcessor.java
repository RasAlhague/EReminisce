package com.rasalhague.ereminisce.processor;

import com.evernote.clients.NoteStoreClient;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.type.Tag;
import com.rasalhague.ereminisce.scanner.service.ServiceDataManager;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;

public class NoteProcessor
{
    private NoteDateCalculator noteDateCalculator = new NoteDateCalculator();
    private NoteMarker         noteMarker;
    private NoteStoreClient    noteStoreClient;
    private ServiceDataManager serviceDataManager;

    public NoteProcessor(NoteStoreClient noteStoreClient, ServiceDataManager serviceDataManager)
    {
        this.noteStoreClient = noteStoreClient;
        this.serviceDataManager = serviceDataManager;
        this.noteMarker = new NoteMarker(noteStoreClient);
    }

    public void processNotes(List<NoteMetadata> noteMetadatas, List<Tag> filteredTags)
    {
        addNewNotesToServiceData(noteMetadatas);
        List<NoteMetadata> ripeNoteMetadatas = noteDateCalculator.extractRipeNotesMetadata(serviceDataManager.getServiceData()
                                                                                                             .getNotesUpdateTime(),
                                                                                           serviceDataManager.getServiceData()
                                                                                                             .getNotesRipeDays(),
                                                                                           noteMetadatas,
                                                                                           filteredTags);
        boolean markResult = noteMarker.mark(ripeNoteMetadatas);
        if (markResult)
        {
            addRipeDaysToServiceData(ripeNoteMetadatas);
        }
    }

    private void addRipeDaysToServiceData(List<NoteMetadata> ripeNoteMetadatas)
    {
        HashMap<String, Long> notesRipeDays = serviceDataManager.getServiceData().getNotesRipeDays();
        ripeNoteMetadatas.forEach((ripeNoteMetadata) -> {

            //for debug
            //            notesRipeDays.put(ripeNoteMetadata.getGuid(), new DateTime().plusDays(700).getMillis());

            notesRipeDays.put(ripeNoteMetadata.getGuid(), new DateTime().getMillis());
        });

        serviceDataManager.updateServiceData();
    }

    private void addNewNotesToServiceData(List<NoteMetadata> noteMetadatas)
    {
        HashMap<String, Long> notesUpdateTime = serviceDataManager.getServiceData().getNotesUpdateTime();
        noteMetadatas.forEach((noteMetadata) -> {

            //if it just added note, put it to service note storage
            //adding with NOW time coz upd date did not update after tag creation (in win version)
            if (!notesUpdateTime.containsKey(noteMetadata.getGuid()))
            {
                notesUpdateTime.put(noteMetadata.getGuid(), new DateTime().getMillis());
            }
        });

        serviceDataManager.updateServiceData();
    }
}
