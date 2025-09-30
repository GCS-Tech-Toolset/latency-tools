package com.gcs.tools.concurrency;

import java.io.IOException;

/**
 * Default implementation using Runtime.getRuntime().exec().
 */
class DefaultCommandRunner implements CommandRunner {
    public Process exec(String command) throws IOException {
        return Runtime.getRuntime().exec(command);
    }
}
