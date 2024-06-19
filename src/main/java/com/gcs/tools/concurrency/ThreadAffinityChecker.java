package com.gcs.tools.concurrency;





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





@Slf4j
public class ThreadAffinityChecker
{



    public static class CommandNotFoundException extends Exception
    {
        public CommandNotFoundException(String message)
        {
            super(message);
        }
    }





    public static void main(String[] args)
    {
        Options options = new Options();
        options.addOption("h", "help", false, "Display help");
        options.addOption("p", "pid", true, "Process ID to check threads for");
        options.addOption("d", "default", false, "Show default system affinity");
        options.addOption("n", "name", true, "Process name to match against");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try
        {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h"))
            {
                formatter.printHelp("ThreadAffinityViaCommand", options);
                return;
            }

            // Ensure necessary commands are available
            ensureCommandAvailable("ps");
            ensureCommandAvailable("taskset");

            if (cmd.hasOption("d"))
            {
                showDefaultAffinity();
                return;
            }


            if (cmd.hasOption("p"))
            {
                String pid = cmd.getOptionValue("p");
                listThreadAffinities(pid);
                showDefaultAffinity();
            }
            else if (cmd.hasOption("n"))
            {
                String processName = cmd.getOptionValue("n");
                findProcessByName(processName);
            }
            else
            {
                _logger.error("No valid option provided. Use -h for help.");
            }
        }
        catch (ParseException e)
        {
            _logger.error("Failed to parse command line options", e);
            formatter.printHelp("ThreadAffinityViaCommand", options);
        }
        catch (Exception e)
        {
            _logger.error("Error executing the program", e);
        }
    }





    private static void ensureCommandAvailable(String command) throws IOException, CommandNotFoundException
    {
        Process process = Runtime.getRuntime().exec("which " + command);
        try
        {
            int exitValue = process.waitFor();
            if (exitValue != 0)
            {
                throw new CommandNotFoundException("Command not found: " + command);
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Command check interrupted", e);
        }
    }





    private static void listThreadAffinities(String pid) throws IOException
    {
        Process psProcess = Runtime.getRuntime().exec("ps -mo pid,tid,%cpu,psr,comm -p " + pid);
        printCommandOutput(psProcess, "Threads and their affinities for PID " + pid + ":");
    }





    private static void findProcessByName(String processName) throws IOException
    {
        Process psProcess = Runtime.getRuntime().exec("ps aux | grep " + processName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(psProcess.getInputStream())))
        {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null)
            {
                _logger.info(line);
                count++;
            }
            if (count > 1)
            {
                _logger.error("Multiple processes found, please specify a more unique identifier");
                System.exit(1);
            }
        }
    }





    private static void showDefaultAffinity() throws IOException
    {
        Process tasksetProcess = Runtime.getRuntime().exec("taskset -p 1");
        printCommandOutput(tasksetProcess, "Default system affinity:");
    }





    private static void printCommandOutput(Process process, String header) throws IOException
    {
        _logger.info(header);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                _logger.info(line);
            }
        }
    }
}
