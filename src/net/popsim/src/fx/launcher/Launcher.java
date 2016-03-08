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
import java.util.concurrent.*;

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
    public void stop() {
        System.out.println("Stopping");
        mTarget.doFinish();
        mTarget.awaitFinish();
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
        System.out.println("show error");
        mStage.hide();
        Stage s = new Stage();
        s.setTitle("Exception");
        s.setScene(makeErrorUi(th));
        s.toFront();
        System.out.println("Show and wait");
        s.showAndWait();
    }

    private void initTarget() {
        System.out.println("Target init");
        mTarget.doInit();
        mWaiter.submit(() -> {
            try {
                mTarget.awaitInit();
                Platform.runLater(this::runTarget);
            } catch (Exception e) {
                handle(e, true);
                stop();
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
                handle(e, true);
            } finally {
                stop();
            }
        });
    }

    private void handle(Throwable th, boolean wait) {
        System.err.println("An error has occurred");
        CountDownLatch latch = wait? new CountDownLatch(1) : null;
        th.printStackTrace();
        Platform.runLater(() -> {
            showErrorStage(th);
            if (wait)
                latch.countDown();
        });
        if (wait) try {
            latch.await();
        } catch (InterruptedException e) {
        } // Oops, but we can't help it
    }
}
