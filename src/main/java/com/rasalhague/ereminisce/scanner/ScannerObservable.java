package com.rasalhague.ereminisce.scanner;

import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.type.Tag;

import java.util.ArrayList;
import java.util.List;

public class ScannerObservable
{
    private List<TaggedNotesLoadedListener> TaggedNotesLoadedListeners = new ArrayList<TaggedNotesLoadedListener>();

    public void addTaggedNotesLoadedListener(TaggedNotesLoadedListener listener)
    {
        TaggedNotesLoadedListeners.add(listener);
    }

    public void removeTaggedNotesLoadedListener(TaggedNotesLoadedListener listener)
    {
        TaggedNotesLoadedListeners.remove(listener);
    }

    public void notifyTaggedNotesLoadedListeners(List<NoteMetadata> noteMetadatas, List<Tag> filteredTags)
    {
        for (TaggedNotesLoadedListener listener : TaggedNotesLoadedListeners)
        {
            listener.onTaggedNotesLoaded(noteMetadatas, filteredTags);
        }
    }

    public interface TaggedNotesLoadedListener
    {
        void onTaggedNotesLoaded(List<NoteMetadata> noteMetadatas, List<Tag> filteredTags);
    }
}
