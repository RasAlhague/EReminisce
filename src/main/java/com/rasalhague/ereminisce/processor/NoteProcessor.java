package com.rasalhague.ereminisce.processor;

import com.evernote.clients.NoteStoreClient;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.type.Tag;
import com.rasalhague.ereminisce.scanner.service.ServiceDataManager;
import org.joda.time.DateTime;

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
        DateTime now            = new DateTime();
        DateTime beginningOfDay = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), 0, 0, 0, 0);

        serviceDataManager.updateWithLastRipes(ripeNoteMetadatas, beginningOfDay);
    }

    private void addNewNotesToServiceData(List<NoteMetadata> noteMetadatas)
    {
        DateTime now            = new DateTime();
        DateTime beginningOfDay = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), 0, 0, 0, 0);

        /**
         * if it just added note, put it to service note storage
         *
         * beginningOfDay has been chosen as date of note creation instead of NOW
         * coz that is convenient when note updates at night (00 00) so, that I can read it (all of them) at morning
         * if it ripe of course
         */
        serviceDataManager.updateWithNewNotes(noteMetadatas, beginningOfDay);
    }
}
