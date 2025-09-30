package com.gcs.tools.concurrency;

import java.io.IOException;

/**
 * Interface for executing system commands.
 */
public interface CommandRunner {
    Process exec(String command) throws IOException;
}
