package com.rasalhague.eremenice.ereminisce.noteanaliser;

import com.evernote.edam.notestore.NoteMetadata;

import java.util.ArrayList;

public interface MatureNotesListener
{
    public void matureNotesFound(ArrayList<NoteMetadata> matureNotes);
}
