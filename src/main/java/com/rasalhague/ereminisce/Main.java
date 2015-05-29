package com.rasalhague.ereminisce;

import org.apache.log4j.Logger;

import javax.swing.*;

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
        try
        {
            logger.info(ClassLoader.getSystemClassLoader().getResource(".").getPath());
            logger.info(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

            JOptionPane.showMessageDialog(null, "infoMessage", "InfoBox: " + "titleBar", JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

//        EvernoteSession evernoteSession = new EvernoteSession();
////        EvernoteSession evernoteSession = new EvernoteSession(EvernoteService.PRODUCTION,
////                                                              "S=s419:U=4427db1:E=154f3a821e5:C=14d9bf6f290:P=1cd:A=en-devtoken:V=2:H=df154c27ae2beb7a994627690f11160a");
//        NoteStoreClient noteStoreClient = evernoteSession.open();
//
//        Scanner scanner = new Scanner(noteStoreClient);
//        ServiceDataManager serviceDataManager = new ServiceDataManager(noteStoreClient);
//        NoteProcessor noteProcessor = new NoteProcessor(noteStoreClient, serviceDataManager);
//
//        scanner.setScannerPeriod(5000);
//
//        Mediator mediator = new Mediator(scanner, noteProcessor);
//        mediator.startScan();
    }
}
