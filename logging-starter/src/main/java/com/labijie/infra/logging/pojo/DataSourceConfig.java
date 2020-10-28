package com.labijie.infra.logging.pojo;

public class DataSourceConfig {
    private String dataSourceName;
    private String table;


    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
