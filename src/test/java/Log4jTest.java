import com.rasalhague.ereminisce.connection.EvernoteSession;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;

public class Log4jTest
{
    private final static Logger logger = Logger.getLogger(EvernoteSession.class);

    @Test
    public void testLogger() throws Exception
    {
        logger.info("Hello from test");

        String filePathString = "./log/log.log";
        File f = new File(filePathString);
        if (!f.exists() || f.isDirectory()) {assert false;}
    }
}
