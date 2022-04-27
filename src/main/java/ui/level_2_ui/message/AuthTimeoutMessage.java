package ui.level_2_ui.message;

public class AuthTimeoutMessage extends AbstractMessage {

    public AuthTimeoutMessage() {
        super(Command.AUTH_TIMEOUT);
    }

    public static AuthTimeoutMessage of() {
        return new AuthTimeoutMessage();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AuthTimeoutMessage{");
        sb.append("timestamp=").append(getTimestamp());
        sb.append('}');
        return sb.toString();
    }
}
