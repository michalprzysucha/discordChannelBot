package com.packt.repositories;

import java.nio.file.Path;
import java.util.List;

public interface ConfigJsonRepository {
        void createGuildConfigFile(Path path);
        void setJsonValue(String key, List<String> values, Path path);
        void setJsonValue(String key, String value, Path path);
        List<String> getJsonValue(String key, Path path);
}
