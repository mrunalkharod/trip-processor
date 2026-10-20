package com.littlepay.assessment.cli;

import com.littlepay.assessment.service.TapFileService;
import com.littlepay.assessment.service.TapProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Profile("cli")
public class TapCommands implements CommandLineRunner, ExitCodeGenerator {

    static final String IMPORT_TAPS = "import-taps";
    static final String PROCESS_TAPS = "process-taps";

    private static final Logger log = LoggerFactory.getLogger(TapCommands.class);

    private final TapFileService tapFileService;
    private final TapProcessingService tapProcessingService;
    private int exitCode;

    public TapCommands(TapFileService tapFileService, TapProcessingService tapProcessingService) {
        this.tapFileService = tapFileService;
        this.tapProcessingService = tapProcessingService;
    }

    @Override
    public void run(String... args) {
        List<String> arguments = Arrays.asList(args);
        Runnable command;
        if (arguments.contains(IMPORT_TAPS)) {
            command = tapFileService::importTaps;
        } else if (arguments.contains(PROCESS_TAPS)) {
            command = tapProcessingService::processTaps;
        } else {
            log.error("Unknown command {}. Usage: --spring.profiles.active=cli {}|{}",
                    arguments, IMPORT_TAPS, PROCESS_TAPS);
            exitCode = 2;
            return;
        }
        try {
            command.run();   // each service logs its own result
            exitCode = 0;
        } catch (RuntimeException e) {
            log.error("Command failed", e);
            exitCode = 1;
        }
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
