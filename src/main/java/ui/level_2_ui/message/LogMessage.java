package ui.level_2_ui.message;

public class LogMessage extends AbstractMessage {

    private final String log;

    private LogMessage(String log) {
        super(Command.LOG);
        this.log = log;
    }

    public String getLog() {
        return log;
    }

    public static LogMessage of(String log) {
        return new LogMessage(log);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LoggerMessage{");
        sb.append("timestamp=").append(getTimestamp());
        sb.append('}');
        return sb.toString();
    }
}
