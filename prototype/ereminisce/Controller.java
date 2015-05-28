package com.rasalhague.eremenice.ereminisce;

import com.evernote.auth.EvernoteService;
import rasalhague.ereminisce.auth.AuthManager;
import rasalhague.ereminisce.noteanaliser.TagStampAnalyzer;
import rasalhague.ereminisce.notesobserver.MarkedTagsObserver;

public class Controller
{
    AuthManager        authManager;
    MarkedTagsObserver markedTagsObserver;
    TagStampAnalyzer   tagStampAnalyzer;

    private final EvernoteService evernoteService = EvernoteService.SANDBOX;
    private final String          DEV_TOKEN       = "S=s1:U=90da5:E=154bd566f51:C=14d65a54318:P=1cd:A=en-devtoken:V=2:H=6255af9a78934f41a9274f698651f05e";

    public static void main(String[] args)
    {
        Controller controller = new Controller();
        controller.init();
        controller.createDependencies();

        controller.authManager.authAsync();
    }

    private void init()
    {
        authManager = new AuthManager(evernoteService, DEV_TOKEN);
        markedTagsObserver = new MarkedTagsObserver();
        tagStampAnalyzer = new TagStampAnalyzer();
    }

    private void createDependencies()
    {
        authManager.addAuthSuccessListener(markedTagsObserver);
        authManager.addAuthSuccessListener(tagStampAnalyzer);
        markedTagsObserver.addMarkedNotesFoundListener(tagStampAnalyzer);
    }
}
