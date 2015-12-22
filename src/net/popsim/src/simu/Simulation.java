package net.popsim.src.simu;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import net.popsim.src.fxui.Context;

import java.util.concurrent.*;

public class Simulation implements EventHandler<KeyEvent> {

    private final Context mContext;
    private final World mWorld;
    private final Canvas mCanvas;
    private final ScheduledExecutorService mTickScheduler;

    private ScheduledFuture mFutureTick;

    public Simulation(Context context) {
        mContext = context;
        mWorld = new World(mContext);
        mCanvas = new Canvas(mWorld.getWidth(), mWorld.getHeight());
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r);
            t.setName("Tick Scheduler");
            return t;
        });
        executor.setRemoveOnCancelPolicy(true);
        mTickScheduler = Executors.unconfigurableScheduledExecutorService(executor);
    }

    public Canvas getCanvas() {
        return mCanvas;
    }

    public void tick() {
        CountDownLatch updateLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            // Todo: Try to use "current" positions and future positions to update and render simultaneously.
            mWorld.render(mCanvas.getGraphicsContext2D());
            updateLatch.countDown();
        });
        try { // Wait for the render to be complete before we jump into the next update
            updateLatch.await();
        } catch (InterruptedException e) {
            System.err.println("Interrupted: tossing update tick");
            return;
        }
        // Todo: Update here
        mWorld.update();
    }

    public void render() {
        GraphicsContext gfx = mCanvas.getGraphicsContext2D();
        mWorld.render(gfx);
    }

    @Override
    public void handle(KeyEvent event) {
        switch (event.getCharacter()) {
            case "p":
                if (isPaused())
                    resume();
                else
                    pause();
            default:
                System.out.println("Typed: " + event.getCharacter());
        }
    }

    public void resume() {
        mFutureTick = mTickScheduler.scheduleAtFixedRate(this::tick, 0, mContext.getTick(), TimeUnit.MILLISECONDS);
    }

    public void pause() {
        if (!isPaused()) {
            mFutureTick.cancel(false);
            mFutureTick = null;
        }
    }

    public boolean isPaused() {
        return mFutureTick == null;
    }

    public void shutdown() {
        pause();
        mTickScheduler.shutdown();
    }
}
