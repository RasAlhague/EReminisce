package com.rasalhague.ereminisce;

import com.evernote.auth.EvernoteService;
import com.evernote.clients.NoteStoreClient;
import com.rasalhague.ereminisce.connection.EvernoteSession;
import com.rasalhague.ereminisce.processor.NoteProcessor;
import com.rasalhague.ereminisce.properties.Properties;
import com.rasalhague.ereminisce.scanner.Scanner;
import com.rasalhague.ereminisce.scanner.service.ServiceDataManager;
import org.apache.log4j.Logger;

public class Main
{
    private final static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args)
    {
        new Main().start(args);
    }

    private void start(String[] args)
    {
        logger.info("Program started");

        Properties properties = new Properties(args);

        //        EvernoteSession evernoteSession = new EvernoteSession(properties);
        EvernoteSession evernoteSession = new EvernoteSession(properties,
                                                              EvernoteService.PRODUCTION,
                                                              "S=s419:U=4427db1:E=154f3a821e5:C=14d9bf6f290:P=1cd:A=en-devtoken:V=2:H=df154c27ae2beb7a994627690f11160a");
        NoteStoreClient noteStoreClient = evernoteSession.open();

        Scanner scanner = new Scanner(noteStoreClient, properties);
        ServiceDataManager serviceDataManager = new ServiceDataManager(noteStoreClient);
        NoteProcessor noteProcessor = new NoteProcessor(noteStoreClient, serviceDataManager);

        Mediator mediator = new Mediator(scanner, noteProcessor);
        mediator.startScan();
    }
}
