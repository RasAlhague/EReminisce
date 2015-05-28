package com.rasalhague.eremenice;

import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.type.Tag;
import com.rasalhague.eremenice.processor.NoteProcessor;
import com.rasalhague.eremenice.scanner.Scanner;
import com.rasalhague.eremenice.scanner.ScannerObservable;

import java.util.List;

public class Mediator implements ScannerObservable.TaggedNotesLoadedListener
{
    private final Scanner       scanner;
    private final NoteProcessor noteProcessor;

    public Mediator(Scanner scanner, NoteProcessor noteProcessor)
    {
        this.scanner = scanner;
        this.noteProcessor = noteProcessor;

        registerEvents();
    }

    @Override
    public void onTaggedNotesLoaded(List<NoteMetadata> noteMetadatas, List<Tag> filteredTags)
    {
        noteProcessor.processNotes(noteMetadatas, filteredTags);
    }

    public void startScan()
    {
        scanner.startSchedule();
    }

    private void registerEvents()
    {
        scanner.getScannerObservable().addTaggedNotesLoadedListener(this);
    }
}
