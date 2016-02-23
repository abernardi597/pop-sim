package net.popsim.src.fx.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import net.popsim.src.simu.Simulation;

import java.io.File;

public class FxUi extends Application {

    protected File mConfigFile;
    private Context mContext;
    private Simulation mSimulation;

    @Override
    public void init() throws Exception {
        System.out.println("Init");
        if (mConfigFile == null)
            mConfigFile = new File(ContextHelper.DIR_HOME, "default.config");
        System.out.println("Using config file " + mConfigFile.getAbsolutePath());
        mContext = ContextHelper.makeContext(mConfigFile);
        mSimulation = new Simulation(mContext);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Start");
        primaryStage.setTitle(ContextHelper.APP_NAME);
        BorderPane pane = new BorderPane();
        pane.setCenter(mSimulation.getCanvas());
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.show();
        mSimulation.begin();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Stop");
        mSimulation.shutdown();
    }
}
