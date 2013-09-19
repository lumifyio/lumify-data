package com.altamiracorp.lumify.model.termMention;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.Row;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;

@Singleton
public class TermMentionRepository extends Repository<TermMention> {
    private TermMentionBuilder termMentionBuilder = new TermMentionBuilder();

    @Inject
    public TermMentionRepository(final ModelSession modelSession) {
        super(modelSession);
    }

    @Override
    public TermMention fromRow(Row row) {
        return termMentionBuilder.fromRow(row);
    }

    @Override
    public Row toRow(TermMention obj) {
        return obj;
    }

    @Override
    public String getTableName() {
        return termMentionBuilder.getTableName();
    }

    public List<TermMention> findByArtifactRowKey(String artifactRowKey, User user) {
        return findByRowStartsWith(artifactRowKey + ":", user);
    }
}
