package com.rasalhague.ereminisce.processor;

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
                                                       HashMap<String, Long> notesLastRipe,
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
                        String noteGUID = noteMetadata.getGuid();
                        Long noteUpdTimeMs = notesUpdateTime.get(noteGUID);
                        Long noteLasRipeMs = notesLastRipe.containsKey(noteGUID) ? notesLastRipe.get(noteGUID) : noteUpdTimeMs;

                        if (checkForRipe(tagInfo, noteUpdTimeMs, noteLasRipeMs))
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

    private boolean checkForRipe(TagInfo tagInfo, Long updTime, Long noteLasRipeMs)
    {
        DateTime now = new DateTime();
        //        DateTime now = new DateTime().plusDays(700);
        //        DateTime now = new DateTime(2015, 5, 29, 22, 0);

        DateTime updDateTime = new DateTime(updTime);
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

            /**
             * when activates between ripe days and have passed last ripe day
             */
            int nearestLowRipeDay = 0;
            for (int ripeDay : ripeDaysFromUpd)
            {
                if (daysBetweenNowAndUpd < ripeDay) break;

                nearestLowRipeDay = ripeDay;
            }
            boolean missedLastRipeAndNeedToMark = noteLasRipeMs < updDateTime.plusDays(nearestLowRipeDay).getMillis();

            /**
             * when activates after ripe days array and have passed last ripe day
             */
            int nearestLowRipeDayAfterArray = daysShiftFromUpd;
            for (int i = daysBetweenNowAndUpd; i != 0; i--)
            {
                if ((i) % (maxDaysGap + 1) == 0)
                {
                    nearestLowRipeDayAfterArray = i;
                    break;
                }
            }
            boolean missedLastRipeAndNeedToMarkAfterArray = noteLasRipeMs <
                    updDateTime.plusDays(nearestLowRipeDayAfterArray).getMillis();

            return containsInRipeDays ||
                    maxRipeDay ||
                    missedLastRipeAndNeedToMark ||
                    missedLastRipeAndNeedToMarkAfterArray;
        }

        return false;
    }
}
