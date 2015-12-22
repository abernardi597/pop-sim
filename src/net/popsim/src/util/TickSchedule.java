package net.popsim.src.util;

import java.util.Arrays;

// Todo: Document
public class TickSchedule {

    private long[] mSchedule;
    private Notifiable mToNotify;

    public TickSchedule(int size, Notifiable toNotify) {
        mSchedule = new long[size];
        Arrays.fill(mSchedule, -1);
        mToNotify = toNotify;
    }

    public void tick(Object... args) {
        for (int i = 0; i < mSchedule.length; i++)
            if (mSchedule[i] >= 0 && mSchedule[i]-- == 0)
                mToNotify.notify(i, args);
    }

    public void schedule(int index, long delay) {
        mSchedule[index] = delay;
    }

    @FunctionalInterface
    public interface Notifiable {

        void notify(int index, Object[] args);
    }
}
