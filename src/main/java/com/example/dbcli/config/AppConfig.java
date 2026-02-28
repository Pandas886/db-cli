package com.example.dbcli.config;

import java.util.ArrayList;
import java.util.List;

public class AppConfig {
    private List<DataSourceConfig> sources = new ArrayList<>();

    public List<DataSourceConfig> getSources() {
        return sources;
    }

    public void setSources(List<DataSourceConfig> sources) {
        this.sources = sources;
    }
}
