import com.rasalhague.ereminisce.connection.EvernoteSession;
import com.rasalhague.ereminisce.properties.Properties;
import com.rasalhague.ereminisce.scanner.service.ServiceData;
import com.rasalhague.ereminisce.scanner.service.ServiceDataManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ServiceManagerTest
{
    ServiceDataManager serviceDataManager;

    @Before
    public void setUp()
    {
        EvernoteSession evernoteSession = new EvernoteSession(new Properties(new String[]{}));
        serviceDataManager = new ServiceDataManager(evernoteSession.open());
    }

    @Test
    public void testGetServiceData()
    {
        ServiceData serviceData = serviceDataManager.getServiceData();

        assertTrue(serviceData.getNotesUpdateTime().containsKey("ServiceNoteCreateDate"));
    }
}
