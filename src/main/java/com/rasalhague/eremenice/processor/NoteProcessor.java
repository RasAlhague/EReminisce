package com.rasalhague.eremenice.processor;

import com.evernote.clients.NoteStoreClient;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.type.Tag;
import com.rasalhague.eremenice.scanner.service.ServiceDataManager;
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
                                                                                           noteMetadatas,
                                                                                           filteredTags);
        markRipeNotes(ripeNoteMetadatas);
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

    private void markRipeNotes(List<NoteMetadata> noteMetadatas)
    {
        noteMarker.mark(noteMetadatas);
    }
}
