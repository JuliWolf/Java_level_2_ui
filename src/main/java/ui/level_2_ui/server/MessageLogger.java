package ui.level_2_ui.server;

import java.io.*;
import org.apache.commons.io.input.*;

public class MessageLogger {
    private final Integer LINES_COUNT = 100;
    private final File file;
    private final String fileName;

    public MessageLogger(String nick) {
        fileName = "history_" + nick;
        file = new File(fileName +".txt");

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write (String line) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String read () {
        final StringBuilder sb = new StringBuilder("");
        try (ReversedLinesFileReader in = new ReversedLinesFileReader(file)) {
            String str;
            int i = 0;
            while ((str = in.readLine()) != null && i < LINES_COUNT) {
                sb.append(str + "\n");
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
