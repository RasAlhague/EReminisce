package com.rasalhague.eremenice;

import com.evernote.clients.NoteStoreClient;
import com.rasalhague.eremenice.connection.EvernoteSession;
import com.rasalhague.eremenice.processor.NoteProcessor;
import com.rasalhague.eremenice.scanner.Scanner;
import com.rasalhague.eremenice.scanner.service.ServiceDataManager;
import org.apache.log4j.Logger;

public class Main
{
    private final static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args)
    {
        new Main().start();
    }

    private void start()
    {
        logger.info("Program started");

        EvernoteSession evernoteSession = new EvernoteSession();
        NoteStoreClient noteStoreClient = evernoteSession.open();

        Scanner scanner = new Scanner(noteStoreClient);
        ServiceDataManager serviceDataManager = new ServiceDataManager(noteStoreClient);
        NoteProcessor noteProcessor = new NoteProcessor(noteStoreClient, serviceDataManager);

        scanner.setScannerPeriod(5000);

        Mediator mediator = new Mediator(scanner, noteProcessor);
        mediator.startScan();
    }
}
