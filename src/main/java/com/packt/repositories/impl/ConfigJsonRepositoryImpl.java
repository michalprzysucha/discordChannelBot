package com.packt.repositories.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.packt.repositories.ConfigJsonRepository;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConfigJsonRepositoryImpl implements ConfigJsonRepository {
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(JsonInclude.Include.ALWAYS);
    private static final Map<String, Lock> readLocks = new ConcurrentHashMap<>();
    private static final Map<String, Lock> writeLocks = new ConcurrentHashMap<>();

    @Override
    public void createGuildConfigFile(Path path) {
        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock(true);
        readLocks.put(path.toString(), reentrantReadWriteLock.readLock());
        writeLocks.put(path.toString(), reentrantReadWriteLock.writeLock());
        if(Files.exists(path)) {
            return;
        }
        try {
            Path configFile = Files.createFile(path);
            ObjectNode jsonNode = MAPPER.createObjectNode();
            jsonNode.putArray("roles");
            jsonNode.putNull("welcome_message");
            jsonNode.putNull("games_category_id");
            jsonNode.putNull("betting_channel_id");
            jsonNode.putNull("rating_channel_id");
            MAPPER.writeValue(Files.newBufferedWriter(configFile), jsonNode);
        } catch (FileAlreadyExistsException e) {
            System.out.println("File already exits: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setJsonValue(String key, String value, Path path) {
        JsonNode root = readJsonRoot(path);
        Lock lock = writeLocks.get(path.toString());
        lock.lock();
        try(var writer = Files.newBufferedWriter(path)){
            ObjectNode objectNode = (ObjectNode) root;
            objectNode.put(key, value);
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(writer, objectNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void setJsonValue(String key, List<String> values, Path path) {
        JsonNode root = readJsonRoot(path);
        Lock lock = writeLocks.get(path.toString());
        lock.lock();
        try(var writer = Files.newBufferedWriter(path)) {
            ObjectNode objectNode = (ObjectNode) root;
            ArrayNode newArrayNode = MAPPER.createArrayNode();
            values.forEach(newArrayNode::add);
            objectNode.set(key, newArrayNode);
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(writer, objectNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public List<String> getJsonValue(String key, Path path) {
        Lock lock = readLocks.get(path.toString());
        lock.lock();
        try (var reader = Files.newBufferedReader(path)) {
            JsonNode node = MAPPER.readTree(reader).get(key);
            if (node.isArray()) {
                TypeReference<List<String>> typeReference = new TypeReference<>() {
                };
                return MAPPER.readValue(node.traverse(), typeReference);
            } else {
                if (node.isNull()) {
                    return null;
                }
                return List.of(node.asText());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            lock.unlock();
        }
    }

    private JsonNode readJsonRoot(Path path) {
        JsonNode root;
        Lock lock = readLocks.get(path.toString());
        lock.lock();
        try (var reader = Files.newBufferedReader(path)) {
            root = MAPPER.readTree(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            lock.unlock();
        }
        return root;
    }
}
