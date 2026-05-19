package com.methum.logstream.storage;

import com.methum.logstream.config.ReadAndWriteConfig;
import com.methum.logstream.ingestion.LogEntry;
import com.methum.logstream.ingestion.LogLevel;
import org.apache.juli.logging.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LogReader {

    private final Path filePath;

    private final ReadAndWriteConfig readAndWriteConfig;

    public LogReader(@Value("${logstream.storage.path}") String filePath, ReadAndWriteConfig readAndWriteConfig) {
        this.filePath = Path.of(filePath);
        this.readAndWriteConfig = readAndWriteConfig;
    }


    public List<LogEntry> read(){

        readAndWriteConfig.readWriteLock().readLock().lock();

        List<LogEntry> logEntries = new ArrayList<LogEntry>();

        try{

            if (!Files.exists(filePath)){
                return new ArrayList<>();
            }


            try(
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(filePath)))

            ){

                while (true){

                    long timestamp;
                    try{
                        timestamp = dis.readLong();
                    }catch (EOFException e){
                        break;
                    }

                    byte level =   dis.readByte();

                    int serviceLength = dis.readInt();

                    byte [] serviceArr = new byte[serviceLength];

                    dis.readFully(serviceArr);

                    int messageLength = dis.readInt();

                    byte [] messageArr = new byte[messageLength];

                    dis.readFully(messageArr);

                    logEntries.add(new LogEntry
                            (Instant.ofEpochMilli(timestamp),
                                    LogLevel.fromNumber(level),
                                    new String(serviceArr,StandardCharsets.UTF_8),
                                    new String(messageArr,StandardCharsets.UTF_8)));


                }
            }

            catch (Exception e) {
                throw new RuntimeException(e);
            }

        } finally {

            readAndWriteConfig.readWriteLock().readLock().unlock();

        }

        return logEntries;

    }


    public LogEntry readAt(long offset) throws IOException {

        readAndWriteConfig.readWriteLock().readLock().lock();

        try(RandomAccessFile randomAccessFile = new RandomAccessFile(filePath.toFile(),"r")){

            randomAccessFile.seek(offset);

            long timestamp = randomAccessFile.readLong();
            System.out.println(timestamp);

            byte level = randomAccessFile.readByte();

            System.out.println(level);
            int serviceLength =randomAccessFile.readInt();
            System.out.println(serviceLength);
            byte [] serviceArr = new byte[serviceLength];

            randomAccessFile.readFully(serviceArr);

            int messageLength = randomAccessFile.readInt();

            System.out.println(messageLength);
            byte [] messageArr = new byte[messageLength];

            randomAccessFile.readFully(messageArr);

            System.out.println(new String(serviceArr,StandardCharsets.UTF_8));
            System.out.println(new String(messageArr,StandardCharsets.UTF_8));
           return new LogEntry(
                   Instant.ofEpochMilli(timestamp),
                   LogLevel.fromNumber(level),
                   new String(serviceArr,StandardCharsets.UTF_8),
                   new String(messageArr,StandardCharsets.UTF_8)
                   );



        }catch(IOException e){
            throw new RuntimeException(e);
        }finally {
            readAndWriteConfig.readWriteLock().readLock().unlock();
        }



    }


    public Map<Long,LogEntry>  readWithOffsets(){

        readAndWriteConfig.readWriteLock().readLock().lock();

        try {

            if (!Files.exists(filePath)) {
                return new HashMap<>();
            }

            Map<Long, LogEntry> longLogEntryMap = new HashMap<>();

            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(filePath)))) {

                long counter = 0;

                while (true) {

                    long timestamp;

                    try {
                        timestamp = dis.readLong();
                    } catch (EOFException e) {
                        break;
                    }
                    byte level = dis.readByte();

                    int serviceLength = dis.readInt();

                    byte[] serviceArr = new byte[serviceLength];

                    dis.readFully(serviceArr);

                    int messageLength = dis.readInt();

                    byte[] messageArr = new byte[messageLength];
                    dis.readFully(messageArr);

                    longLogEntryMap.put(counter, new LogEntry(Instant.ofEpochMilli(timestamp), LogLevel.fromNumber(level), new String(serviceArr, StandardCharsets.UTF_8), new String(messageArr, StandardCharsets.UTF_8)));

                    counter += 8 + 1 + 4 + serviceLength + 4 + messageLength;


                }


            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return longLogEntryMap;
        } finally {
            readAndWriteConfig.readWriteLock().readLock().unlock();
        }









    }
}
