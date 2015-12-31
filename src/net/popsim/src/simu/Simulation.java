package net.popsim.src.simu;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import net.popsim.src.fxui.Context;

import java.util.concurrent.*;

public class Simulation implements EventHandler<KeyEvent> {

    private final Context mContext;
    private final World mWorld;
    private final Canvas mCanvas;
    private final ScheduledExecutorService mTickScheduler;

    private ScheduledFuture mFutureTick;
    private boolean hasBegun;

    public Simulation(Context context) {
        mContext = context;
        mWorld = new World(mContext);
        mCanvas = new Canvas(mWorld.getWidth(), mWorld.getHeight());
        mCanvas.setOnKeyTyped(this);
        mCanvas.setFocusTraversable(true);
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
        // Get the world ready for an update
        mWorld.preUpdate();
        CountDownLatch finalizeLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            mWorld.render(mCanvas.getGraphicsContext2D());
            // Let the update finalize
            finalizeLatch.countDown();
        });
        // Update while rendering
        mWorld.update();
        try {
            // Wait for the render to be complete before we finalize the update
            finalizeLatch.await();
        } catch (InterruptedException e) {
            System.err.println("Interrupted: tossing update tick");
            return;
        }
        // Finalize the update, setting current positions to future ones
        mWorld.postUpdate();
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

    public void begin() {
        if (hasBegun) {
            System.err.println("Simulation has already begun");
            return;
        }
        hasBegun = true;
        System.out.printf("Beginning simulation @ %dHz\n", mContext.getTickFrequency());
        mWorld.init();
        //resume();
    }

    public void resume() {
        mFutureTick = mTickScheduler.scheduleAtFixedRate(this::tick, 0, mContext.getTickInterval(), TimeUnit.NANOSECONDS);
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
