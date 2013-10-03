package com.altamiracorp.lumify.storm;

import java.util.ArrayList;
import java.util.List;

public class TextExtractedInfo {
    private List<TermMention> termMentions = new ArrayList<TermMention>();

    public void addAll(List<TermMention> termMentions) {
        this.termMentions.addAll(termMentions);
    }

    public void mergeFrom(TextExtractedInfo result) {
        this.termMentions.addAll(result.termMentions);
    }

    public List<TermMention> getTermMentions() {
        return termMentions;
    }

    public static class TermMention {

        private final int start;
        private final int end;
        private final String sign;
        private final String ontologyClassUri;

        public TermMention(int start, int end, String sign, String ontologyClassUri) {
            this.start = start;
            this.end = end;
            this.sign = sign;
            this.ontologyClassUri = ontologyClassUri;
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
    }
}
