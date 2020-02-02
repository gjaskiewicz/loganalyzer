package com.creditsuisse.loganalyzer;

import java.io.File;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the program - reads command line args and passes them to the runner.
 */
public class Entrypoint {
    final static Logger logger = LoggerFactory.getLogger(Entrypoint.class);

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(Option.builder("t")
            .longOpt("threads")
            .desc("number of threads to use")
            .hasArg()
            .type(Number.class)
            .argName("THREADS")
            .build());
        
        options.addOption(Option.builder("a")
            .longOpt("alert")
            .desc("alert threshold to flag logs")
            .hasArg()
            .type(Number.class)
            .argName("THRESHOLD")
            .build());
        
        options.addOption(Option.builder("h")
            .longOpt("help")
            .desc("display help")
            .build());
     
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            
            if (cmd.hasOption('h')) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("loganalyzer", options);
                return;
            }
            
            List<String> positionalArgs = cmd.getArgList();
            if (positionalArgs.isEmpty()) {
                logger.error("File name not provided");
                return;
            }
            String fileName = positionalArgs.get(0);
            File fileToAnalyze = new File(fileName);
            if (!fileToAnalyze.exists()) {
                logger.error("File not exists");
                return;
            }
            
            AnalyzeRunner.Builder analyzerBuilder = new AnalyzeRunner.Builder(fileToAnalyze);
            
            if (cmd.hasOption('t')) {
                int threads = ((Number)cmd.getParsedOptionValue("t")).intValue();
                analyzerBuilder.withThreads(threads);
            }
            
            if (cmd.hasOption('a')) {
                long timeAlertThreshold = ((Number)cmd.getParsedOptionValue("a")).longValue();
                analyzerBuilder.withTimeThreshold(timeAlertThreshold);
            }
            
            AnalyzeRunner analyzer = analyzerBuilder.build();
            analyzer.analyzeFile();
            
        } catch (ParseException e) {
            logger.error("Error while parsing options", e);
        }
    }
}
