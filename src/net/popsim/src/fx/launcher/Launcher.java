package net.popsim.src.fx.launcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Launcher extends Application {

    private static final int SPLASH_WIDTH = 320;
    private static final int SPLASH_HEIGHT = 240;
    private static final int ERROR_WIDTH = 600;
    private static final int ERROR_HEIGHT = 800;
    private static Target TARGET;

    public static void setTarget(Target target) {
        TARGET = target;
    }

    private final Target mTarget;
    private final ExecutorService mWaiter;
    private Stage mStage;
    private Text mLogo;
    private Label mTitle;
    private Label mDetail;
    private ProgressIndicator mProgress;

    public Launcher() {
        super();
        if (TARGET == null)
            throw new IllegalStateException("Launcher has no target");
        else mTarget = TARGET;
        mWaiter = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Launcher Wait Thread");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void init() throws Exception {
        System.out.println("Launcher init");
        mLogo = new Text(mTarget.getName());
        mLogo.setFont(Font.font(Font.getDefault().getName(), FontWeight.EXTRA_BOLD, 32));
        mTitle = new Label();
        mTitle.setFont(Font.font(12));
        mTitle.setPadding(new Insets(0, 3, 0, 3));
        mDetail = new Label();
        mDetail.setFont(Font.font(12));
        mDetail.setPadding(new Insets(0, 3, 0, 3));
        mDetail.setDisable(true);
        mProgress = new ProgressIndicator();
        mProgress.setMaxSize(100, 100);
        mProgress.setMaxWidth(SPLASH_WIDTH);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Launcher start");
        mStage = primaryStage;
        mStage.setResizable(false);
        mStage.initStyle(StageStyle.UNDECORATED);
        mStage.setScene(makeSplashUi());
        mStage.show();
        mTitle.textProperty().bind(mTarget.mTitle);
        mDetail.textProperty().bind(mTarget.mMessage);
        Platform.runLater(this::initTarget);
    }

    @Override
    public void stop() throws Exception {
        mTarget.doFinish();
        mTarget.awaitFinish();
        System.out.println("Stopping");
        System.exit(0);
    }

    private Scene makeSplashUi() {
        BorderPane root = new BorderPane();
        root.setPrefSize(SPLASH_WIDTH, SPLASH_HEIGHT);
        VBox v = new VBox(mLogo, mProgress);
        v.setAlignment(Pos.CENTER);
        v.setSpacing(10);
        root.setCenter(v);
        root.setBottom(new VBox(mTitle, mDetail));
        return new Scene(root);
    }

    private Scene makeErrorUi(Throwable th) {
        BorderPane root = new BorderPane();
        root.setPrefSize(ERROR_WIDTH, ERROR_HEIGHT);
        TextArea log = new TextArea();
        StringWriter s = new StringWriter();
        th.printStackTrace(new PrintWriter(s));
        log.setText(s.toString());
        log.setEditable(false);
        ScrollPane pane = new ScrollPane(log);
        pane.setFitToHeight(true);
        pane.setFitToWidth(true);
        // Todo: Finish launcher error window
        root.setCenter(pane);
        return new Scene(root);
    }

    private void showErrorStage(Throwable th) {
        Platform.setImplicitExit(false);
        mStage.hide();
        Stage s = new Stage();
        s.setTitle("Exception");
        s.setScene(makeErrorUi(th));
        s.toFront();
        s.showAndWait();
        Platform.setImplicitExit(true);
    }

    private void initTarget() {
        System.out.println("Target init");
        mTarget.doInit();
        mWaiter.submit(() -> {
            try {
                mTarget.awaitInit();
                Platform.runLater(this::runTarget);
            } catch (Exception e) {
                handle(e);
            }
        });
    }

    private void runTarget() {
        System.out.println("Target start");
        Platform.setImplicitExit(false);
        mTarget.doMain();
        mStage.hide();
        mWaiter.submit(() -> {
            try {
                mTarget.awaitMain();
            } catch (Exception e) {
                handle(e);
            } finally {
                Platform.setImplicitExit(true);
            }
        });
    }

    private void handle(Throwable th) {
        System.err.println("An error has occurred");
        th.printStackTrace();
        Platform.runLater(() -> showErrorStage(th));
    }
}
