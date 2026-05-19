package com.methum.logstream;


import com.methum.logstream.ingestion.LogEntry;
import com.methum.logstream.ingestion.LogLevel;
import com.methum.logstream.storage.LogReader;
import com.methum.logstream.storage.LogWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
public class ThreadsPoolTest{

    private final Path testFilePath;

    private final LogWriter logWriter;

    @Autowired
    private LogReader logReader;

    @Autowired
    public ThreadsPoolTest(@Value("${logstream.storage.path}") String testFilePath, LogWriter logWriter) {
        this.testFilePath = Path.of(testFilePath);
        this.logWriter = logWriter;
    }



    @BeforeEach
    public void createTestFile(){

        System.out.println(testFilePath);

        if (!Files.exists(testFilePath.getParent())){
            try{
                Files.createDirectory(testFilePath.getParent());
                Files.createFile(testFilePath);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else if (!Files.exists(testFilePath)){
            try{
                Files.createFile(testFilePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }



    }

    @Test
    public void testWriteEndpointWithMultipleThreads(){

        List<Future<?>> futersList = new ArrayList<>();

        try (ExecutorService executorService = Executors.newFixedThreadPool(5)){

            for (int i = 0; i < 5; i++) {

                LogEntry logEntry = new LogEntry(Instant.now(),LogLevel.INFO,"service" + i,"message" + i);

               Future<?> future =  executorService.submit(()-> {
                    try {
                        logWriter.write(logEntry);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

               futersList.add(future);

            }

            for (Future<?> future : futersList) {
                future.get();
            }



        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<LogEntry> logEntries = new ArrayList<>();

        logEntries = logReader.read();

        assertEquals(5,logEntries.size());

    }

//    @AfterEach
//    public void clearTestFile(){
//        try{
//            Files.delete(testFilePath);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }



}
