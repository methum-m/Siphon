package com.methum.logstream.storage;

import com.methum.logstream.config.ReadAndWriteConfig;
import com.methum.logstream.ingestion.LogEntry;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.io.UTF8Writer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class LogWriter {

    private static final Logger log = LoggerFactory.getLogger(LogWriter.class);
    private final Path filepath;

    private final InvertedIndex invertedIndex;

    private final ReadAndWriteConfig readAndWriteConfig;


    public LogWriter(@Value("${logstream.storage.path}") String filePath, InvertedIndex invertedIndex, ReadAndWriteConfig readAndWriteConfig){

        this.filepath = Path.of(filePath);
        this.invertedIndex = invertedIndex;
        this.readAndWriteConfig = readAndWriteConfig;
    }



    public void write(LogEntry logEntry) throws IOException {

        readAndWriteConfig.readWriteLock().writeLock().lock();

        try {

            try (DataOutputStream dos = new DataOutputStream
                    (new BufferedOutputStream
                            (Files.newOutputStream(filepath, StandardOpenOption.APPEND)))) {

                byte[] serviceBytes = logEntry.service().getBytes(StandardCharsets.UTF_8);
                byte[] messageBytes = logEntry.message().getBytes(StandardCharsets.UTF_8);

                long  fileOffset = Files.size(filepath);


                dos.writeLong(logEntry.timestamp().toEpochMilli());
                dos.writeByte(logEntry.level().getNumber());
                dos.writeInt(serviceBytes.length);
                dos.write(serviceBytes);
                dos.writeInt(messageBytes.length);
                dos.write(messageBytes);


                invertedIndex.index(logEntry,fileOffset);



            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            readAndWriteConfig.readWriteLock().writeLock().unlock();
        }
    }


    @PostConstruct
    public void ensureStorageExists(){


        if (!Files.exists(filepath.getParent())){
            try{
                Files.createDirectory(filepath.getParent());
                Files.createFile(filepath);

             } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else if(!Files.exists(filepath)){

            try {
                Files.createFile(filepath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }



    }


}
