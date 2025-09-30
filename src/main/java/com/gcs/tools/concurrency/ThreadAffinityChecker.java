package com.gcs.tools.concurrency;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


@Data
@Slf4j
public class ThreadAffinityChecker {

    // the actual runner implementation
    private static CommandRunner commandRunner = new DefaultCommandRunner();





    /**
     * Entry point for the thread affinity checker utility.
     *
     * @param args Command line arguments
     */
    public static void main(final String[] args) {

        // Define CLI options
        Options options = new Options();
        options.addOption("h", "help", false, "Display help");
        options.addOption("p", "pid", true, "Process ID to check threads for");
        options.addOption("d", "default", false, "Show default system affinity");
        options.addOption("n", "name", true, "Process name to match against");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmdLine = parser.parse(options, args);

            // Show help if requested
            if (cmdLine.hasOption("h")) {
                formatter.printHelp("ThreadAffinityViaCommand", options);
                return;
            }

            // Ensure required system commands are available
            ensureCommandAvailable("ps");
            ensureCommandAvailable("taskset");

            // Show default affinity if requested
            if (cmdLine.hasOption("d")) {
                showDefaultAffinity();
                return;
            }

            // Query by PID
            if (cmdLine.hasOption("p")) {
                String processId = cmdLine.getOptionValue("p");
                listThreadAffinities(processId);
                showDefaultAffinity();
            }
            // Query by process name
            else if (cmdLine.hasOption("n")) {
                String processName = cmdLine.getOptionValue("n");
                findProcessByName(processName);
            }
            // No valid option provided
            else {
                log.error("No valid option provided. Use -h for help.");
            }
        } catch (ParseException parseEx) {
            log.error("Failed to parse command line options", parseEx);
            formatter.printHelp("ThreadAffinityViaCommand", options);
        } catch (Exception ex) {
            log.error("Error executing the program", ex);
        }
    }





    /**
     * Ensures a system command is available in the environment.
     * Logs command execution and errors, uses descriptive variable names, and ensures resources are closed.
     *
     * @param commandName Name of the command to check
     * @throws IOException              If an I/O error occurs
     * @throws CommandNotFoundException If the command is not found
     */
    public static void ensureCommandAvailable(final String commandName) throws IOException, CommandNotFoundException {
        log.debug("Checking availability of command: {}", commandName);

        Process whichProcess = commandRunner.exec("which " + commandName);
        try {
            final int whichExitCode = whichProcess.waitFor();
            if (whichExitCode != 0) {
                log.error("Command not found: {} (exit code: {})", commandName, whichExitCode);
                throw new CommandNotFoundException("Command not found: " + commandName);
            }

            // Optionally log the resolved path
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(whichProcess.getInputStream()))) {
                String resolvedPath = reader.readLine();
                if (resolvedPath != null && !resolvedPath.isEmpty()) {
                    log.debug("Command '{}' resolved to: {}", commandName, resolvedPath);
                }
            }
        } catch (InterruptedException interruptedEx) {
            Thread.currentThread().interrupt();
            log.error("Command check interrupted for: {}", commandName, interruptedEx);
            throw new RuntimeException("Command check interrupted", interruptedEx);
        }
    }





    /**
     * Lists thread affinities for a given process ID.
     *
     * @param processId Process ID to query
     * @throws IOException If an I/O error occurs
     */
    public static void listThreadAffinities(final String processId) throws IOException {
        Process psProcess = commandRunner.exec("ps -mo pid,tid,%cpu,psr,comm -p " + processId);
        printCommandOutput(psProcess, "Threads and their affinities for PID " + processId + ":");
    }





    /**
     * Finds processes by name and prints their details.
     * Exits if multiple processes are found.
     *
     * @param processName Name of the process to search for
     * @throws IOException If an I/O error occurs
     */
    public static void findProcessByName(final String processName) throws IOException {
        Process psProcess = commandRunner.exec("ps aux | grep " + processName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(psProcess.getInputStream()))) {

            String line;
            int processCount = 0;

            while ((line = reader.readLine()) != null) {
                log.info(line);
                processCount++;
            }

            if (processCount > 1) {
                log.error("Multiple processes found, please specify a more unique identifier");
                System.exit(1);
            }
        }
    }





    /**
     * Shows the default system affinity using taskset.
     *
     * @throws IOException If an I/O error occurs
     */
    public static void showDefaultAffinity() throws IOException {
        Process tasksetProcess = commandRunner.exec("taskset -p 1");
        printCommandOutput(tasksetProcess, "Default system affinity:");
    }





    /**
     * Prints the output of a system command with a header.
     *
     * @param process Process whose output to print
     * @param header  Header message to display
     * @throws IOException If an I/O error occurs
     */
    public static void printCommandOutput(final Process process, final String header) throws IOException {
        log.info(header);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {
                log.info(line);
            }
        }
    }
}
