package net.popsim.src.fx.launcher;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import net.popsim.src.util.ExceptionalRunnable;
import net.popsim.src.util.Setter;

import java.util.concurrent.*;

public abstract class Target {

    protected StringProperty mTitle;
    protected StringProperty mMessage;
    private final Coalescer mCoalescer;
    private final ExecutorService mAppExecutor;
    private CountDownLatch mInitLatch;
    private CountDownLatch mMainLatch;
    private CountDownLatch mFinishLatch;
    private Exception mException;

    public Target() {
        mTitle = new SimpleStringProperty();
        mMessage = new SimpleStringProperty();
        mCoalescer = new Coalescer();
        mAppExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, getName()));
    }

    public abstract void init() throws Exception;

    public abstract void main() throws Exception;

    public abstract void finish();

    public abstract String getName();

    public void doInit() {
        if (mInitLatch != null)
            throw new IllegalStateException("Already started init");
        mInitLatch = new CountDownLatch(1);
        mAppExecutor.submit(tryTo(this::init, mInitLatch::countDown));
    }

    public void awaitInit() throws Exception {
        if (mInitLatch == null)
            throw new IllegalStateException("Haven't started init");
        mInitLatch.await();
        if (mException != null)
            throw new Exception("Exception during init", mException);
    }

    public void doMain() {
        if (mMainLatch != null)
            throw new IllegalStateException("Already started main");
        mMainLatch = new CountDownLatch(1);
        mAppExecutor.submit(tryTo(this::main, mMainLatch::countDown));
    }

    public void awaitMain() throws Exception {
        if (mMainLatch == null)
            throw new IllegalStateException("Haven't started main");
        mMainLatch.await();
        if (mException != null)
            throw new Exception("Exception during main", mException);
    }

    public void doFinish() {
        if (mFinishLatch != null)
            throw new IllegalStateException("Already started finish");
        mFinishLatch = new CountDownLatch(1);
        mAppExecutor.submit(tryTo(this::finish, mFinishLatch::countDown));
    }

    public void awaitFinish() {
        if (mFinishLatch == null)
            throw new IllegalStateException("Haven't started finish");
        try {
            mFinishLatch.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while waiting for finish", e);
        }
    }

    public void updateTitle(String title) {
        if (title != null && !title.isEmpty())
            System.out.println(title);
        mCoalescer.set(mCoalescer::setTitle, title);
    }

    public void updateMessage(String msg) {
        if (msg != null && !msg.isEmpty())
            System.out.println("    " + msg);
        mCoalescer.set(mCoalescer::setMessage, msg);
    }

    public void update(String title, String msg) {
        updateTitle(title);
        updateMessage(msg);
    }

    protected void runOnFx(boolean wait, Runnable run) throws InterruptedException {
        final CountDownLatch latch = wait? new CountDownLatch(1) : null;
        Platform.runLater(!wait? run : () -> {
            run.run();
            latch.countDown();
        });
        if (wait)
            latch.await();
    }

    private Runnable tryTo(ExceptionalRunnable run, Runnable after) {
        return () -> {
            try {
                run.run();
            } catch (Exception e) {
                mException = e;
            } finally {
                if (after != null)
                    after.run();
            }
        };
    }

    private class Coalescer implements Runnable {

        private String mTitle;
        private String mMessage;
        private boolean isRun, hasSchedule;
        private CountDownLatch mLatch;

        public Coalescer() {
            reset();
        }

        public void run() {
            isRun = true;
            synchronized (this) {
                if (mTitle != null)
                    Target.this.mTitle.set(mTitle);
                if (mMessage != null)
                    Target.this.mMessage.set(mMessage);
            }
            mLatch.countDown();
        }

        public void reset() {
            mTitle = mMessage = null;
            isRun = false;
            hasSchedule = false;
            mLatch = new CountDownLatch(1);
        }

        public void schedule() {
            if (hasSchedule)
                return;
            hasSchedule = true;
            Platform.runLater(this);
        }

        public void awaitAndReset() {
            try {
                mLatch.await();
                reset();
            } catch (InterruptedException ignored) {
            }
        }

        public synchronized void setTitle(String title) {
            mTitle = title;
        }

        public synchronized void setMessage(String message) {
            mMessage = message;
        }

        protected <T> void set(Setter<T> setter, T toSet) {
            if (hasSchedule && isRun)
                awaitAndReset();
            setter.set(toSet);
            if (!hasSchedule)
                schedule();
        }
    }
}
