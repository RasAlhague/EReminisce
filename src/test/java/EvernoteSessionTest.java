import com.evernote.clients.NoteStoreClient;
import com.rasalhague.ereminisce.connection.EvernoteSession;
import com.rasalhague.ereminisce.properties.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

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
        evernoteSession = new EvernoteSession(new Properties(new String[]{}));
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
