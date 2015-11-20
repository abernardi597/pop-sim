package net.popsim.src.internal.io;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Splits output via the normal print* methods into two different OutputStreams
 */
public class SplitPrintStream extends PrintStream {

    /**
     * Handle to the other PrintStream. The OutputStream that this also prints to is used as the underlying stream.
     */
    private final PrintStream mOtherStream;

    /**
     * Creates a SplitPrintStream on the given streams.
     *
     * @param out   the underlying OutputStream
     * @param other the other OutputStream to write to
     */
    public SplitPrintStream(OutputStream out, OutputStream other) {
        super(out, true);
        mOtherStream = new PrintStream(other, true);
    }

    // Just pass off write operations to the other PrintStream

    @Override
    public void write(int b) {
        super.write(b);
        mOtherStream.write(b);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);
        mOtherStream.write(buf, off, len);
    }
}
