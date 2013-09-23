package com.altamiracorp.lumify.model;

public abstract class BaseBuilder<TModel> {
    public abstract TModel fromRow(Row row);

    public abstract String getTableName();

}
