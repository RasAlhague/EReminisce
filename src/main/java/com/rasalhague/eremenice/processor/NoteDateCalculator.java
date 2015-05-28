package com.rasalhague.eremenice.processor;

import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.type.Tag;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoteDateCalculator
{
    private final String                   TAG_PARSE_REGEX = "(?<ER>ER) (?<DelayK>\\d+)";
    private       HashMap<String, TagInfo> tagsInfo        = new HashMap<>();
    private       int                      maxDaysGap      = 30;

    public void setMaxDaysGap(int maxDaysGap)
    {
        this.maxDaysGap = maxDaysGap;
    }

    public List<NoteMetadata> extractRipeNotesMetadata(HashMap<String, Long> notesUpdateTime,
                                                       List<NoteMetadata> noteMetadatas,
                                                       List<Tag> tags)
    {
        List<NoteMetadata> ripeNotesMetadata = new ArrayList<>();

        for (NoteMetadata noteMetadata : noteMetadatas)
        {
            for (String tagGUID : noteMetadata.getTagGuids())
            {
                for (Tag tag : tags)
                {
                    if (tagGUID.equals(tag.getGuid()))
                    {
                        TagInfo tagInfo = parseTagName(tag);

                        if (checkForRipe(tagInfo, notesUpdateTime.get(noteMetadata.getGuid())))
                        {
                            ripeNotesMetadata.add(noteMetadata);
                        }
                    }
                }
            }
        }

        return ripeNotesMetadata;
    }

    //              TagGUID
    private TagInfo parseTagName(Tag tag)
    {
        if (!tagsInfo.containsKey(tag.getGuid()))
        {
            Matcher matcher = Pattern.compile(TAG_PARSE_REGEX).matcher(tag.getName());
            if (matcher.find())
            {
                float delayK = Float.parseFloat(matcher.group("DelayK"));
                tagsInfo.put(tag.getGuid(), new TagInfo(tag.getGuid(), delayK));
            }
        }

        return tagsInfo.get(tag.getGuid());
    }

    private boolean checkForRipe(TagInfo tagInfo, Long updTime)
    {
        DateTime updDateTime = new DateTime(updTime);
        DateTime now = new DateTime();
        //        DateTime now = new DateTime().plusDays(526);
        //        DateTime now = new DateTime(2015, 5, 29, 22, 0);
        ArrayList<Integer> ripeDaysFromUpd = new ArrayList<>();
        float delayK = tagInfo.getDelayK();
        int daysGap = 0;
        int daysShiftFromUpd = 0;
        int iterator = 0;

        if (delayK > 0)
        {
            while (daysGap < maxDaysGap)
            {
                daysGap = (int) (iterator * delayK);
                daysShiftFromUpd = daysShiftFromUpd + daysGap + 1;

                ripeDaysFromUpd.add(daysShiftFromUpd);

                iterator++;
            }

            int daysBetweenNowAndUpd = Days.daysBetween(updDateTime, now).getDays();
            boolean containsInRipeDays = ripeDaysFromUpd.contains(daysBetweenNowAndUpd);
            boolean maxRipeDay = (daysBetweenNowAndUpd > ripeDaysFromUpd.get(ripeDaysFromUpd.size() - 1)) &&
                    ((daysBetweenNowAndUpd - daysShiftFromUpd) % (maxDaysGap + 1) == 0);

            return containsInRipeDays || maxRipeDay;
        }

        return false;
    }
}
