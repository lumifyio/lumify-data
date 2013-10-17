package com.altamiracorp.lumify.core.model;

import com.altamiracorp.lumify.core.model.Row;

public abstract class BaseBuilder<TModel> {
    public abstract TModel fromRow(Row row);

    public abstract String getTableName();

}
