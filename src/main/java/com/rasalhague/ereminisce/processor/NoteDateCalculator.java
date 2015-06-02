package com.rasalhague.ereminisce.processor;

import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.type.Tag;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NoteDateCalculator
{
    private final static Logger                   logger          = Logger.getLogger(NoteDateCalculator.class);
    private final        String                   TAG_PARSE_REGEX = "(?<ER>ER) (?<DelayK>\\d+)";
    private              HashMap<String, TagInfo> tagsInfo        = new HashMap<>();
    private              int                      maxDaysGap      = 30;

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

                        logger.info("Check ripe for " + noteMetadata);
                        if (checkForRipe(tagInfo, noteUpdTimeMs, noteLasRipeMs))
                        {
                            ripeNotesMetadata.add(noteMetadata);
                        }
                    }
                }
            }
        }

        logger.info("So, ripe notes are: " +
                            ripeNotesMetadata.stream().map(NoteMetadata::getTitle).collect(Collectors.toList()) + "\n");

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

    /**
     * main fucking logic
     */
    private boolean checkForRipe(TagInfo tagInfo, Long updTime, Long noteLasRipeMs)
    {
        DateTime now = new DateTime();

        /**
         * For Debug
         *
         * IMPORTANT: if you wont to use this, you must change debug value in ServiceDataManager.updateWithLastRipes()
         */
        //        DateTime now = new DateTime().plusDays(700);
        //        DateTime now = new DateTime(2015, 5, 29, 22, 0);
        // ---

        DateTime           updDateTime      = new DateTime(updTime);
        ArrayList<Integer> ripeDaysFromUpd  = new ArrayList<>();
        float              delayK           = tagInfo.getDelayK();
        int                daysGap          = 0;
        int                daysShiftFromUpd = 0;
        int                iterator         = 0;

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
            boolean outOfRipeDaysArray = (daysBetweenNowAndUpd > ripeDaysFromUpd.get(ripeDaysFromUpd.size() - 1));
            boolean hasBeenRipedToday = Days.daysBetween(new DateTime(noteLasRipeMs), now).getDays() == 0;

            boolean containsInRipeDays = ripeDaysFromUpd.contains(daysBetweenNowAndUpd) && !hasBeenRipedToday;
            boolean ripeDayOutOfRipeDaysArray = outOfRipeDaysArray &&
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
             * when activates between ripe days and have passed last ripe day AND out of ripe array
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
            boolean missedLastRipeAndNeedToMarkAfterArray = (noteLasRipeMs <
                    updDateTime.plusDays(nearestLowRipeDayAfterArray).getMillis()) && outOfRipeDaysArray;

            logger.info("containsInRipeDays: " + containsInRipeDays);
            logger.info("ripeDayOutOfRipeDaysArray: " + ripeDayOutOfRipeDaysArray);
            logger.info("missedLastRipeAndNeedToMark: " + missedLastRipeAndNeedToMark);
            logger.info("missedLastRipeAndNeedToMarkAfterArray: " + missedLastRipeAndNeedToMarkAfterArray);

            return containsInRipeDays ||
                    ripeDayOutOfRipeDaysArray ||
                    missedLastRipeAndNeedToMark ||
                    missedLastRipeAndNeedToMarkAfterArray;
        }

        return false;
    }
}
