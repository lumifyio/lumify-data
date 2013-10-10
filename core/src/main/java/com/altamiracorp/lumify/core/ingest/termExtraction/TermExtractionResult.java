package com.altamiracorp.lumify.core.ingest.termExtraction;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.collect.Lists;

public class TermExtractionResult {
    private final List<TermMention> termMentions = Lists.newArrayList();

    public void add(TermMention termMention) {
        checkNotNull(termMention);

        termMentions.add(termMention);
    }

    public void addAll(List<TermMention> mentions) {
        checkNotNull(mentions);

        termMentions.addAll(mentions);
    }

    public void mergeFrom(TermExtractionResult result) {
        checkNotNull(result);
        checkNotNull(result.termMentions);

        termMentions.addAll(result.termMentions);
    }

    public List<TermMention> getTermMentions() {
        return termMentions;
    }

    public static class TermMention {

        private final int start;
        private final int end;
        private final String sign;
        private final String ontologyClassUri;
        private final boolean resolved;

        public TermMention(int start, int end, String sign, String ontologyClassUri, boolean resolved) {
            this.start = start;
            this.end = end;
            this.sign = sign;
            this.ontologyClassUri = ontologyClassUri;
            this.resolved = resolved;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public String getSign() {
            return sign;
        }

        public String getOntologyClassUri() {
            return ontologyClassUri;
        }

        public boolean isResolved() {
            return resolved;
        }
    }
}
