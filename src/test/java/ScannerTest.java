import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.type.Tag;
import com.rasalhague.eremenice.connection.EvernoteSession;
import com.rasalhague.eremenice.scanner.Scanner;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ScannerTest
{
    Scanner scanner;

    @Before
    public void before() throws Exception
    {
        EvernoteSession evernoteSession = new EvernoteSession();
        scanner = new Scanner(evernoteSession.open());
    }

    @Test(timeout = 3000)
    public void testLoadTagList() throws Exception
    {
        Method method = scanner.getClass().getDeclaredMethod("loadTagList");
        method.setAccessible(true);
        List<Tag> tagList = (List<Tag>) method.invoke(scanner);

        System.out.println("\n\n");
        System.out.println("tagList: " + tagList);
        System.out.println("\n\n");

        assertTrue(tagList.size() > 0);
    }

    @Test(timeout = 3000)
    public void testLoadTaggedNotes() throws Exception
    {
        List<NoteMetadata> notesMetadataList = scanner.loadTaggedNotes();

        System.out.println("\n\n");
        System.out.println("notesMetadataList: " + notesMetadataList);
        System.out.println("\n\n");

        assertTrue(notesMetadataList.size() > 0);
    }

    @Test
    public void testScannerObservable()
    {
        scanner.setScannerPeriod(1000);
        scanner.getScannerObservable().addTaggedNotesLoadedListener((noteMetadatas, tagList) -> {
            System.out.println(noteMetadatas);
            System.out.println();
            assertTrue(noteMetadatas.size() > 0);
        });
        scanner.startSchedule();
        try
        {
            Thread.sleep(4000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
