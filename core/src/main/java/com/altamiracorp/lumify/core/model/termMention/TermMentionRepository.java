package com.altamiracorp.lumify.core.model.termMention;

import com.altamiracorp.lumify.core.model.ModelSession;
import com.altamiracorp.lumify.core.model.Repository;
import com.altamiracorp.lumify.core.model.Row;
import com.altamiracorp.lumify.core.user.User;
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

    public List<TermMention> findByGraphVertexId(String graphVertexId, User user) {
        return findByRowStartsWith(graphVertexId + ":", user);
    }
}
