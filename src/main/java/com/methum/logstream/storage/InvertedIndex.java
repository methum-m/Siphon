package com.methum.logstream.storage;

import com.methum.logstream.config.ReadAndWriteConfig;
import com.methum.logstream.ingestion.LogEntry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
public class InvertedIndex {

    private final LogReader logReader;

    private final ObjectMapper objectMapper;

    private final Path filePath;

    private final ReadAndWriteConfig readAndWriteConfig;

    HashMap<String, List<Long>> termOffsets = new HashMap<String,List<Long>>();

    public InvertedIndex(LogReader logReader, ObjectMapper objectMapper, @Value("${logstream.index.storage.path}") String filePath, ReadAndWriteConfig readAndWriteConfig) {
        this.logReader = logReader;
        this.objectMapper = objectMapper;
        this.filePath = Path.of(filePath);
        this.readAndWriteConfig = readAndWriteConfig;
    }

    public void index(LogEntry logEntry,long offset){

        // get the individual terms from the message
        String [] terms = logEntry.message().split("\\s+");


        for (String term : terms) {

            termOffsets.computeIfAbsent(term, k -> new ArrayList<>()).add(offset);

        }

    }


    public List<LogEntry> search(String term) throws IOException {

        readAndWriteConfig.readWriteLock().readLock().lock();

        List<LogEntry> entries = new ArrayList<>();


        try {

            if (termOffsets.containsKey(term)) {

                List<Long> offsets = termOffsets.get(term);

                for (Long offset : offsets) {
                    LogEntry logEntry = logReader.readAt(offset);
                    entries.add(logEntry);
                }

            } else {

                return new ArrayList<>();
            }
        }finally{
            readAndWriteConfig.readWriteLock().readLock().unlock();
        }


        return entries;

    }

    @PreDestroy
    public void persistIndex(){

        try{
            objectMapper.writeValue(filePath,termOffsets);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }


    }

    @PostConstruct
    public void rebuildIndex(){

        if (Files.exists(filePath)){

            // construct the type for List<Long>
            // construct the type for termOffset HashMap<String,List<Long>>
            // read from the filePath and load into the termOffset hashmap

        JavaType offsetType = objectMapper.getTypeFactory().constructCollectionType(List.class,Long.class);

        JavaType termType = objectMapper.getTypeFactory().constructType(String.class);

        JavaType type = objectMapper.getTypeFactory().constructMapType(Map.class,termType,offsetType);

        try {
            termOffsets = objectMapper.readValue(filePath,type);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }


        }else{
            try {
                Files.createFile(filePath);

                Map<Long,LogEntry> logEntryMap = logReader.readWithOffsets();

                Set<Long> offsets = logEntryMap.keySet();

                for (long offset : offsets) {

                    index(logEntryMap.get(offset), offset);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }



    }





}







