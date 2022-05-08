package ui.level_2_ui.server;

import java.util.concurrent.Callable;

public class AuthTimeout implements Callable<Boolean> {
    private final int AUTH_TIMEOUT;

    public AuthTimeout(int timeoutSec) {
        AUTH_TIMEOUT = timeoutSec;
    }

    @Override
    public Boolean call() throws Exception {
        Thread.sleep(AUTH_TIMEOUT * 1000);

        return true;
    }
}
