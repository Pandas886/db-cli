package com.example.dbcli.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ConfigLoader {
    private ConfigLoader() {
    }

    public static Map<String, DataSourceConfig> loadSources(Path configPath) {
        if (!FileUtil.exist(configPath.toFile())) {
            throw new IllegalArgumentException("Config file not found: " + configPath);
        }

        String raw = FileUtil.readString(configPath.toFile(), StandardCharsets.UTF_8);
        AppConfig cfg = JSONUtil.toBean(raw, AppConfig.class);

        if (cfg == null || cfg.getSources() == null || cfg.getSources().isEmpty()) {
            throw new IllegalArgumentException("No sources defined in: " + configPath);
        }

        return cfg.getSources().stream()
                .peek(ConfigLoader::validateSource)
                .collect(Collectors.toMap(DataSourceConfig::getName, Function.identity(), (a, b) -> b));
    }

    private static void validateSource(DataSourceConfig source) {
        if (isBlank(source.getName())) {
            throw new IllegalArgumentException("A source is missing 'name'");
        }
        if (isBlank(source.getJdbcUrl())) {
            throw new IllegalArgumentException("Source '" + source.getName() + "' is missing 'jdbcUrl'");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
