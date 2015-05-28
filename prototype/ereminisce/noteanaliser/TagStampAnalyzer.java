package com.rasalhague.eremenice.ereminisce.noteanaliser;

import com.evernote.clients.NoteStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Tag;
import com.evernote.thrift.TException;
import org.joda.time.DateTime;
import org.joda.time.Days;
import rasalhague.ereminisce.auth.AuthManager;
import rasalhague.ereminisce.notesobserver.TagsToAnalyzeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagStampAnalyzer implements TagsToAnalyzeListener, AuthManager.AuthSuccessListener
{
    private NoteStoreClient noteStore;
    private       RepeatRowCalculator       repeatRowCalculator   = new RepeatRowCalculator();
    private final String                    TAG_FILTER            = "(?<ER>ER):(?<FirstReminisce>\\d+):(?<Ratio>\\d+(.?\\d+)?)";
    private final String                    FIRST_REMINISCE_GROUP = "FirstReminisce";
    private final String                    RATIO_GROUP           = "Ratio";
    private final String                    REPEAT_TAG_NAME       = "To Repeat";
    private       List<MatureNotesListener> MatureNotesListeners  = new ArrayList<MatureNotesListener>();

    public ArrayList<Tag> extractMatureTags(ArrayList<Tag> tagsToAnalyze)
    {
        NotesMetadataResultSpec notesMetadataResultSpec = new NotesMetadataResultSpec();
        notesMetadataResultSpec.setIncludeUpdated(true);
        notesMetadataResultSpec.setIncludeAttributes(true);
        notesMetadataResultSpec.setIncludeTitle(true);
        notesMetadataResultSpec.setIncludeTagGuids(true);

        List<String> tagsGIUD = extractTagsGUID(tagsToAnalyze);

        for (int i = 0; i < tagsToAnalyze.size(); i++)
        {
            String guid = tagsGIUD.get(i);
            Tag tagToAnalyze = tagsToAnalyze.get(i);

            /**
             * getting tag info
             */
            int firstReminisce = 0;
            float ratio = 0;

            Matcher matcher = Pattern.compile(TAG_FILTER).matcher(tagToAnalyze.getName());
            if (matcher.find())
            {
                firstReminisce = Integer.parseInt(matcher.group(FIRST_REMINISCE_GROUP));
                ratio = Float.parseFloat(matcher.group(RATIO_GROUP));
            }
            ERTagInfo erTagInfo = new ERTagInfo(firstReminisce, ratio);

            /**
             * setting up filter
             */
            ArrayList<String> guidL = new ArrayList<String>();
            guidL.add(guid);

            NoteFilter noteFilter = new NoteFilter();
            noteFilter.setTagGuids(guidL);

            try
            {
                //get notes according to filter
                NotesMetadataList notesMetadata = noteStore.findNotesMetadata(noteFilter,
                                                                              0,
                                                                              Short.MAX_VALUE,
                                                                              notesMetadataResultSpec);

                for (NoteMetadata noteMetadata : notesMetadata.getNotes())
                {
                    long updated = noteMetadata.getUpdated();

                    DateTime dt1 = new DateTime(updated);
                    DateTime dt2 = new DateTime();
                    int daysBetween = Days.daysBetween(dt1, dt2).getDays();

                    System.out.println(daysBetween);
                    if (repeatRowCalculator.calculateRow(erTagInfo).contains(daysBetween))
                    {
                        /**
                         * check for tag existing
                         */
                        Tag repeatTag = null;

                        List<Tag> tagList1 = noteStore.listTags();
                        for (Tag tag : tagList1)
                        {
                            if (tag.getName().equals(REPEAT_TAG_NAME))
                            {
                                repeatTag = tag;
                                break;
                            }
                        }

                        if (repeatTag == null)
                        {
                            repeatTag = new Tag();
                            repeatTag.setName(REPEAT_TAG_NAME);
                            noteStore.createTag(repeatTag);
                        }

                        /**
                         * getting tag guid
                         */
                        List<Tag> tags = noteStore.listTags();
                        for (Tag tag : tags)
                        {
                            if (tag.getName().equals(REPEAT_TAG_NAME))
                            {
                                repeatTag = tag;
                                break;
                            }
                        }
                        List<String> listWithRepeatTag = new ArrayList<String>();
                        listWithRepeatTag.add(repeatTag.getGuid());

                        /**
                         * update note with new "Repeat" tag
                         */
                        Note note = noteStore.getNote(noteMetadata.getGuid(), false, false, false, false);
                        //save previous tags
                        listWithRepeatTag.addAll(note.getTagGuids());
                        note.setTagGuids(listWithRepeatTag);
                        noteStore.updateNote(note);
                    }
                }

            }
            catch (EDAMUserException e)
            {
                e.printStackTrace();
            }
            catch (EDAMSystemException e)
            {
                e.printStackTrace();
            }
            catch (EDAMNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (TException e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

    private List<String> extractTagsGUID(List<Tag> tagList)
    {
        ArrayList<String> tagsGUID = new ArrayList<String>();

        for (Tag tag : tagList)
        {
            tagsGUID.add(tag.getGuid());
        }

        return tagsGUID;
    }

    @Override
    public void tagsToAnalyzeFound(ArrayList<Tag> tagsToAnalyze)
    {
        extractMatureTags(tagsToAnalyze);
    }

    public void addMatureNotesListener(MatureNotesListener listener) { MatureNotesListeners.add(listener);}

    public void removeMatureNotesListener(MatureNotesListener listener) { MatureNotesListeners.remove(listener);}

    public void notifyMatureNotesListeners(ArrayList<NoteMetadata> matureNotes)
    {
        for (MatureNotesListener listener : MatureNotesListeners)
        {
            listener.matureNotesFound(matureNotes);
        }
    }

    @Override
    public void authSucceed(NoteStoreClient noteStore)
    {
        this.noteStore = noteStore;
    }
}
