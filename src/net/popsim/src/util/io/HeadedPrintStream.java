package net.popsim.src.util.io;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

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
     * Header format for new threads.
     */
    private static final String HEADER_THREAD_NEW = "%s (%d)";
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
     * The name of the last Thread to print to this stream. When a different Thread prints, a nifty header is printed.
     */
    private String mLastThread;

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
        String name = current.getName();
        long id = current.getId();
        // If the map doesn't contain the id or the names do not match
        if (!name.equals(mLastThread)) {
            // Update the last thread
            mLastThread = name;
            // Print that a new thread has printed
            printf(HEADER_THREAD_NEW, name, id);
            println();
            // Rewrite the header so we can have a normal line
            writeHeader();
        } else print("  ");
    }
}
