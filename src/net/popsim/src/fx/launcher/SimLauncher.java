package net.popsim.src.fx.launcher;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.*;
import net.popsim.src.fx.ui.Context;
import net.popsim.src.fx.ui.ContextHelper;
import net.popsim.src.simu.Simulation;

import java.io.File;

public class SimLauncher extends Target {

    public static void main(String[] args) {
        Launcher.setTarget(new SimLauncher());
        Launcher.launch(Launcher.class, args);
    }

    private File mConfigFile;
    private Context mContext;
    private Simulation mSimulation;

    @Override
    public void init() throws Exception {
        updateTitle("Init log output");
        ContextHelper.setupOutput();
        updateTitle("Init compiler");
        ContextHelper.setupCompiler();
        update("Loading configuration", "Choosing file");
        runOnFx(true, () -> {
            FileChooser f = new FileChooser();
            f.setInitialDirectory(ContextHelper.DIR_HOME);
            mConfigFile = f.showOpenDialog(null);
        });
        if (mConfigFile == null) {
            update("Aborting", "");
            return;
        }
        updateMessage("Making context with " + mConfigFile.getAbsolutePath());
        mContext = ContextHelper.makeContext(mConfigFile);
        mSimulation = new Simulation(mContext);
    }

    @Override
    public void main() throws Exception {
        if (mContext == null)
            return;
        runOnFx(true, () -> {
            Stage stage = new Stage(StageStyle.UNIFIED);
            stage.setTitle(String.format("%s (%s)", getName(), mConfigFile.getAbsolutePath()));
            BorderPane root = new BorderPane();
            root.setCenter(mSimulation.getCanvas());
            Scene s = new Scene(root);
            stage.setScene(s);
            stage.show();
        });
        mSimulation.begin();
    }

    @Override
    public void finish() {
        System.out.println("finish");
        mSimulation.shutdown();
    }

    @Override
    public String getName() {
        return "PopSim";
    }
}
