package net.popsim.src.internal.io;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;

public class HeadedPrintStream extends PrintStream {

    /**
     * The header of the day. Formatted as
     * <br><tt>[ DD:MM:YYYY ]</tt>
     */
    private static final String HEADER_DAY = "[ %1$td-%1$tm-%1$tY ]";
    /**
     * The header of the time. Formatted as
     * <br><tt>[HH:MM:SS:mmm]</tt>
     */
    private static final String HEADER_TIME = "[%1$tH:%1$tM:%1$tS:%1$tL] ";
    /**
     * Header format for the current thread ID.
     */
    private static final String HEADER_THREAD = "(%4d)  ";
    /**
     * Header format for the
     */
    private static final String HEADER_THREAD_NEW = "(%4d) %s";
    /**
     * The number of milliseconds in a day. This is used to calculate when a day passes and the day header should be
     * reprinted.
     */
    private static final long MILLIS_PER_DAY = 1000 * 60 * 60 * 24;

    /**
     * A flag used when checking if a header must be written. This allows us to "inject" a header before a line is written.
     */
    private boolean mNeedsHeader;
    /**
     * A map used to track which Threads have used this object. When a Thread prints with this stream for the first time,
     * a special message is printed to show the thread name with its ID.
     */
    private HashMap<Long, String> mThreadMap;
    /**
     * Days since the epoch. Used to keep track of when day headers should be printed.
     */
    private long mDay;

    /**
     * @see PrintStream#PrintStream(OutputStream, boolean)
     */
    public HeadedPrintStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
        mNeedsHeader = true;
        mThreadMap = new HashMap<>();
    }

    // Overridden to make sure we catch newlines

    public void write(int b) {
        super.write(b);
        mNeedsHeader = b == '\n';
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        // Inject header
        if (mNeedsHeader) {
            buf = Arrays.copyOfRange(buf, off, len);
            off = 0;
            writeHeader();
        }
        // Write the original stuff
        super.write(buf, off, len);
        // Buffers with new lines typically end with them...
        mNeedsHeader = len > 0 && buf[off + len - 1] == '\n';
    }

    /**
     * Prints the header.
     */
    private void writeHeader() {
        // Make sure we don't just keep printing headers
        mNeedsHeader = false;
        // Get the current time
        long t = System.currentTimeMillis();
        // Calculate the day
        long day = t / MILLIS_PER_DAY;
        // Check if we need to print a day header
        if (mDay != day) {
            mDay = day;
            printf(HEADER_DAY, t);
            println();
            // Printing the newline character will prompt another header to be written: make sure this does not happen
            mNeedsHeader = false;
        }
        // Print the time header
        printf(HEADER_TIME, t);
        // Get the current thread and its ID
        Thread current = Thread.currentThread();
        long id = current.getId();
        String s = mThreadMap.get(id);
        // If the map doesn't contain the id or the names do not match
        if (s == null || !s.equals(current.getName())) {
            // Add it/Update the map
            mThreadMap.put(id, s = current.getName());
            // Print that a new thread has printed
            printf(HEADER_THREAD_NEW, id, s);
            println();
            // Rewrite the header so we can have a normal line
            writeHeader();
        }
        else printf(HEADER_THREAD, id); // Everything was normal, so we just print the normal thread header
    }
}
