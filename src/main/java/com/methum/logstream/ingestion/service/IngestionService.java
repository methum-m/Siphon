package com.methum.logstream.ingestion.service;

import com.methum.logstream.ingestion.LogEntry;
import com.methum.logstream.ingestion.dtos.LogEntryRequestDto;
import com.methum.logstream.storage.LogWriter;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class IngestionService {

    private final LogWriter logWriter;

    public IngestionService(LogWriter logWriter){
        this.logWriter = logWriter;
    }

    public void handleLogEntryRequest(LogEntryRequestDto logEntryRequestDto) {

        LogEntry logEntry = new LogEntry(Instant.now(),logEntryRequestDto.level(),logEntryRequestDto.service(),logEntryRequestDto.message());

        logWriter.write(logEntry);

    }
}
