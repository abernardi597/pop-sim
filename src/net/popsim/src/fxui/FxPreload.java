package net.popsim.src.fxui;

import com.sun.javafx.application.LauncherImpl;
import javafx.application.Preloader;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class FxPreload extends Preloader {

    public static void launch(String... args) {
        try {
            ContextHelper.setupOutput();
            ContextHelper.setupCompiler();
            LauncherImpl.launchApplication(FxUi.class, FxPreload.class, args);
        } catch (Exception e) {
            throw new RuntimeException("Fatal exception", e);
        } finally {
            System.out.println("Closing");
        }
    }

    private File mConfigFile;

    @Override
    public void init() throws Exception {
        System.out.println("Preload init");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Preload start");
        // Todo: Check program arguments for config file
        // Todo: Show a window that will let the user choose what to do
        FileChooser chooser = new FileChooser();
        mConfigFile = chooser.showOpenDialog(primaryStage);
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        switch (info.getType()) {
            case BEFORE_LOAD:
                System.out.println("Pre-load");
                break;
            case BEFORE_INIT:
                System.out.println("Pre-init");
                ((FxUi) info.getApplication()).mConfigFile = mConfigFile;
                break;
            case BEFORE_START:
                System.out.println("Pre-start");
                break;
        }
    }
}
