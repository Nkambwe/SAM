package com.pbu.sam.configuration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.pbu.sam.utils.Literals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
@Configuration
public class LogbackConfiguration {
    private static final String LOG_FILE_PATH = Literals.LOG_FILEPATH;
    private static final String LOG_FILE_NAME = Literals.LOG_FILENAME;
    private static final Level LOG_LEVEL = Level.INFO;
    private final Logger logger = LoggerFactory.getLogger(LogbackConfiguration.class);
    public LogbackConfiguration() {
        configureLogger();
    }
    private void configureLogger() {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

            //region Configure console appender
            ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
            consoleAppender.setContext(loggerContext);
            consoleAppender.setName("CONSOLE");
            PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
            consoleEncoder.setContext(loggerContext);
            consoleEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} %-5level [%thread] - %msg%n");
            consoleEncoder.start();
            consoleAppender.setEncoder(consoleEncoder);
            consoleAppender.start();
            //endregion

            //region Configure rolling file appender
            RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
            rollingFileAppender.setContext(loggerContext);
            rollingFileAppender.setName("FILE");
            String logFileName = getLogFileName();
            rollingFileAppender.setFile(Paths.get(LOG_FILE_PATH, logFileName).toString());
            //endregion

            //region Configure rolling policy
            SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
            rollingPolicy.setContext(loggerContext);
            rollingPolicy.setParent(rollingFileAppender);
            rollingPolicy.setFileNamePattern(Paths.get(LOG_FILE_PATH, "Agent_Logs.%d{yyyy-MM-dd}.%i.txt").toString());
            rollingPolicy.setMaxHistory(30); // Keep 30 days of logs
            rollingPolicy.setMaxFileSize(FileSize.valueOf("50MB")); // Set max file size
            rollingPolicy.start();
            rollingFileAppender.setRollingPolicy(rollingPolicy);
            //endregion

            //region Configure encoder
            PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
            fileEncoder.setContext(loggerContext);
            fileEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} %-5level [%thread] - %msg%n");
            fileEncoder.start();
            rollingFileAppender.setEncoder(fileEncoder);
            rollingFileAppender.start();
            //endregion

            //region Get the root logger and add appends
            ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(LOG_LEVEL);
            rootLogger.addAppender(consoleAppender);
            rootLogger.addAppender(rollingFileAppender);
            logger.info("Logback configured successfully.");
            //endregion

        } catch (Exception e) {
            logger.error("Failed to configure Logback.", e);
        }
    }
    private String getLogFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(new Date());
        return String.format("%s %s.txt", LOG_FILE_NAME, formattedDate);
    }
}
