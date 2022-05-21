package ui.level_2_ui.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.level_2_ui.message.AbstractMessage;

public class ServerLogger {
    private final Logger logger;

    public ServerLogger () {
        this.logger =  LogManager.getLogger(ServerLogger.class);
    }

    public void info (String message) {
        logger.info(message);
    }

    public void info (AbstractMessage message) {
        logger.info(message);
    }

    public void error (String message) {
        logger.error(message);
    }

    public void fatal (String message) {
        logger.fatal(message);
    }

    public void warn (String message) {
        logger.warn(message);
    }

    public void debug (String message) {
        logger.debug(message);
    }

}
