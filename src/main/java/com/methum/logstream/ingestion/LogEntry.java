package com.methum.logstream.ingestion;

import java.time.Instant;

public record LogEntry(
        Instant timestamp,
        LogLevel level,
        String service,
        String message
) {}
