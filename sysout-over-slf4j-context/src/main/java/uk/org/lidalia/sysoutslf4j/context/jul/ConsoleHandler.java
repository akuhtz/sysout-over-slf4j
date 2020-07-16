package uk.org.lidalia.sysoutslf4j.context.jul;

import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import uk.org.lidalia.sysoutslf4j.system.PerContextSystemOutput;

public class ConsoleHandler extends StreamHandler {

    /**
     * Create a <code>ConsoleHandler</code> for <code>System.err</code>.
     * <p>
     * The <code>ConsoleHandler</code> is configured based on
     * <code>LogManager</code> properties (or their default values).
     *
     */
    public ConsoleHandler() {
        setOutputStream(PerContextSystemOutput.ERR.getOriginalPrintStream());
    }

    /**
     * Publish a <code>LogRecord</code>.
     * <p>
     * The logging request was made initially to a <code>Logger</code> object,
     * which initialized the <code>LogRecord</code> and forwarded it here.
     * <p>
     * @param  record  description of the log event. A null record is
     *                 silently ignored and is not published
     */
    public void publish(LogRecord record) {
        super.publish(record);
        flush();
    }

    /**
     * Override <code>StreamHandler.close</code> to do a flush but not
     * to close the output stream.  That is, we do <b>not</b>
     * close <code>System.err</code>.
     */
    public void close() {
        flush();
    }
}

