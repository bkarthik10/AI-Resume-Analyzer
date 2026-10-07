package com.resumeanalyzer.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Reads the JSON reference data from the project's data/ folder. */
@Component
public class DataFiles {

    private final ObjectMapper mapper;
    private final Path dir;

    public DataFiles(ObjectMapper mapper, @Value("${app.data-dir:../data}") String dataDir) {
        this.mapper = mapper;
        Path p = Path.of(dataDir);
        if (!Files.isDirectory(p) && Files.isDirectory(Path.of("data"))) p = Path.of("data");
        this.dir = p;
    }

    public <T> T read(String fileName, TypeReference<T> type) throws IOException {
        Path file = dir.resolve(fileName);
        if (!Files.exists(file)) {
            throw new IOException("Data file not found: " + file.toAbsolutePath()
                    + " (run the backend from the backend/ folder or set DATA_DIR)");
        }
        return mapper.readValue(file.toFile(), type);
    }
}
