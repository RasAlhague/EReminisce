import com.evernote.clients.NoteStoreClient;
import com.rasalhague.eremenice.connection.EvernoteSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * EvernoteSession Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>мая 27, 2015</pre>
 */
public class EvernoteSessionTest
{
    EvernoteSession evernoteSession;

    @Before
    public void before() throws Exception
    {
        evernoteSession = new EvernoteSession();
    }

    @After
    public void after() throws Exception
    {
    }

    @Test(timeout = 5000)
    public void testOpen() throws Exception
    {
        NoteStoreClient noteStoreClient = evernoteSession.open();
        assertNotNull(noteStoreClient);
    }
} 
