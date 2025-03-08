package com.packt.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerConfigFileLocks {
    private static final Map<String, Lock> readLocks = new ConcurrentHashMap<>();
    private static final Map<String, Lock> writeLocks = new ConcurrentHashMap<>();

    public static void initializeLocks(){
        Path currentDirectory = Paths.get(".");
        try(var stream = Files.list(currentDirectory)){
            stream
                    .filter(path -> Files.isRegularFile(path) &&
                            path.getFileName().toString().endsWith(".json") &&
                            !path.getFileName().toString().equals("config.json"))
                    .forEach(path -> {
                        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock(true);
                        readLocks.put(path.getFileName().toString(), reentrantReadWriteLock.readLock());
                        writeLocks.put(path.getFileName().toString(), reentrantReadWriteLock.writeLock());
                    });
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static Map<String, Lock> getReadLocks(){
        return readLocks;
    }

    public static Map<String, Lock> getWriteLocks(){
        return writeLocks;
    }

    public static void addReadLock(Path path, Lock readLock){
        readLocks.put(path.toString(), readLock);
    }

    public static void addWriteLock(Path path, Lock writeLock){
        writeLocks.put(path.toString(), writeLock);
    }
}
