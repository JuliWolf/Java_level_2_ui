package ui.level_2_ui.message;

public class ChangeNickMessage extends AbstractMessage {
    private final String oldNick;
    private final String newNick;

    public ChangeNickMessage(String oldNick, String newNick) {
        super(Command.CHANGE_NICK);

        this.oldNick = oldNick;
        this.newNick = newNick;
    }

    public String getOldNick() {
        return oldNick;
    }

    public String getNewNick() {
        return newNick;
    }

    public static ChangeNickMessage of(String oldNick, String newNick) {
        return new ChangeNickMessage(oldNick, newNick);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChangeNickMessage{");
        sb.append("timestamp=").append(getTimestamp());
        sb.append(", oldNick='").append(oldNick).append('\'');
        sb.append(", newNick='").append(newNick).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
