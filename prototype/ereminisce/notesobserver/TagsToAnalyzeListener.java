package com.rasalhague.eremenice.ereminisce.notesobserver;

import com.evernote.edam.type.Tag;

import java.util.ArrayList;

public interface TagsToAnalyzeListener
{
    public void tagsToAnalyzeFound(ArrayList<Tag> tagsToAnalyze);
}
