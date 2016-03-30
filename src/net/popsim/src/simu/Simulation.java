package net.popsim.src.simu;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import net.popsim.src.util.ExceptionalRunnable;

import java.util.concurrent.*;

public class Simulation implements ExceptionalRunnable {

    private final Context mContext;
    private final World mWorld;
    private final Canvas mCanvas;
    private final ScheduledExecutorService mTickScheduler;

    private ScheduledFuture mFutureTick;
    private CountDownLatch mFinishLatch;
    private Exception mException;

    public Simulation(Context context) throws Exception {
        mContext = context;
        mWorld = mContext.getWorldClass().getConstructor(Simulation.class, Context.class).newInstance(this, mContext);
        mCanvas = new Canvas(mWorld.getWidth(), mWorld.getHeight());
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
        try {
            // Get the world ready for an update
            mWorld.preUpdate();
            CountDownLatch finalizeLatch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    mWorld.render(mCanvas.getGraphicsContext2D());
                } catch (Exception e) {
                    mException = new Exception("Exception during simulation render tick", e);
                }
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
            // Finalize the update if everything goes smoothly
            if (mException == null)
                mWorld.postUpdate();
        } catch (Exception e) {
            mException = new Exception("Exception during simulation update tick", e);
        }
        if (mException != null)
            signalShutdown();
    }

    @Override
    public void run() throws Exception {
        begin();
        try {
            mFinishLatch.await();
        } catch (InterruptedException e) {
            throw new Exception("Interrupted while awaiting termination", e);
        }
        if (mException != null)
            throw mException;
        shutdown();
    }

    public void begin() throws Exception {
        if (mFinishLatch != null) {
            System.err.println("Simulation has already begun");
            return;
        }
        mFinishLatch = new CountDownLatch(1);
        System.out.printf("Beginning simulation @ %dHz\n", mContext.getTickFrequency());
        mWorld.init();
        resume();
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

    public void signalShutdown() {
        if (mFinishLatch == null)
            throw new IllegalStateException("Simulation has not started");
        else mFinishLatch.countDown();
    }

    public void shutdown() {
        pause();
        signalShutdown();
        mTickScheduler.shutdown();
    }
}
