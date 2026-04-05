package com.methum.logstream.ingestion.dtos;

import com.methum.logstream.ingestion.LogLevel;

public record LogEntryRequestDto(

       LogLevel level,
       String service,
       String message

) {}
