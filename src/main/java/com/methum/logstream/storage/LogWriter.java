package com.methum.logstream.storage;

import com.methum.logstream.ingestion.LogEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class LogWriter {

    private final Path filepath;

    public LogWriter(@Value("${logstream.storage.path}") String filePath){

        this.filepath = Path.of(filePath);
    }

    public void write(LogEntry logEntry){

    }
}
