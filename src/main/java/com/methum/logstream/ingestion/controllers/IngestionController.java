package com.methum.logstream.ingestion.controllers;

import com.methum.logstream.ingestion.dtos.LogEntryRequestDto;
import com.methum.logstream.ingestion.service.IngestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IngestionController {


    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService){
        this.ingestionService = ingestionService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<String> ingestLog(@RequestBody LogEntryRequestDto logEntryRequestDto){

        ingestionService.handleLogEntryRequest(logEntryRequestDto);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }


}
