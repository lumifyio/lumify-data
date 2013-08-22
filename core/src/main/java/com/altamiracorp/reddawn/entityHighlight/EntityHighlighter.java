package com.altamiracorp.reddawn.entityHighlight;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRepository;
import com.altamiracorp.reddawn.ucd.sentence.SentenceTerm;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermAndTermMention;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class EntityHighlighter {
    private TermRepository termRepository = new TermRepository();
    private SentenceRepository sentenceRepository = new SentenceRepository();

    public String getHighlightedText(RedDawnSession session, Artifact artifact) {
        try {
            Collection<Term> terms = termRepository.findByArtifactRowKey(session.getModelSession(), artifact.getRowKey().toString());
            List<OffsetItem> termAndTermMetadata = getTermAndTermMetadataForArtifact(artifact.getRowKey(), terms);
            List<OffsetItem> sentences = getSentencesForArtifact(session, artifact.getRowKey());

            ArrayList<OffsetItem> offsetItems = new ArrayList<OffsetItem>();
            offsetItems.addAll(termAndTermMetadata);
            offsetItems.addAll(sentences);

            return getHighlightedText(artifact.getContent().getDocExtractedTextString(), 0, offsetItems);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getHighlightedText(String text, int textStartOffset, List<OffsetItem> offsetItems) throws JSONException {
        Collections.sort(offsetItems, new OffsetItemComparator());
        StringBuilder result = new StringBuilder();
        PriorityQueue<Integer> endOffsets = new PriorityQueue<Integer>();
        int lastStart = textStartOffset;
        for (int i = 0; i < offsetItems.size(); i++) {
            OffsetItem offsetItem = offsetItems.get(i);

            boolean overlapsPreviousItem = false;
            if (offsetItem instanceof TermAndTermMentionOffsetItem) {
                for (int j = 0; j < i; j++) {
                    OffsetItem compareItem = offsetItems.get(j);
                    if (compareItem instanceof TermAndTermMentionOffsetItem && (compareItem.getEnd() >= offsetItem.getEnd()
                            || compareItem.getEnd() > offsetItem.getStart())) {
                        overlapsPreviousItem = true;
                        offsetItems.remove(i--);
                        break;
                    }
                }
            }
            if (overlapsPreviousItem) {
                continue;
            }
            if (offsetItem.getStart() < textStartOffset || offsetItem.getEnd() < textStartOffset) {
                continue;
            }
            if (!offsetItem.shouldHighlight()) {
                continue;
            }

            while (endOffsets.size() > 0 && endOffsets.peek() <= offsetItem.getStart()) {
                int end = endOffsets.poll();
                result.append(text.substring(lastStart - textStartOffset, end - textStartOffset));
                result.append("</span>");
                lastStart = end;
            }
            result.append(text.substring(lastStart - textStartOffset, (int) (offsetItem.getStart() - textStartOffset)));

            JSONObject infoJson = offsetItem.getInfoJson();

            result.append("<span");
            result.append(" class=\"");
            result.append(StringUtils.join(offsetItem.getCssClasses(), " "));
            result.append("\"");
            if (offsetItem.getTitle() != null) {
                result.append(" title=\"");
                result.append(StringEscapeUtils.escapeHtml(offsetItem.getTitle()));
                result.append("\"");
            }
            result.append(" data-info=\"");
            result.append(StringEscapeUtils.escapeHtml(infoJson.toString()));
            result.append("\"");
            result.append(">");
            endOffsets.add((int) offsetItem.getEnd());
            lastStart = (int) offsetItem.getStart();
        }

        while (endOffsets.size() > 0) {
            int end = endOffsets.poll();
            result.append(text.substring(lastStart - textStartOffset, end - textStartOffset));
            result.append("</span>");
            lastStart = end;
        }
        result.append(text.substring(lastStart - textStartOffset));

        return result.toString();
    }

    public List<OffsetItem> getSentencesForArtifact(RedDawnSession session, ArtifactRowKey rowKey) {
        List<Sentence> sentences = sentenceRepository.findByArtifactRowKey(session.getModelSession(), rowKey);
        ArrayList<OffsetItem> sentenceOffsetItems = new ArrayList<OffsetItem>();
        for (Sentence sentence : sentences) {
            sentenceOffsetItems.add(new SentenceOffsetItem(sentence));
        }
        return sentenceOffsetItems;
    }

    public static List<OffsetItem> getTermAndTermMetadataForArtifact(ArtifactRowKey artifactKey, Collection<Term> terms) {
        ArrayList<OffsetItem> termMetadataOffsetItems = new ArrayList<OffsetItem>();
        for (Term term : terms) {
            for (TermMention termMention : term.getTermMentions()) {
                if (termMention.getArtifactKey().equals(artifactKey.toString())) {
                    termMetadataOffsetItems.add(new TermAndTermMentionOffsetItem(new TermAndTermMention(term, termMention)));
                }
            }
        }
        return termMetadataOffsetItems;
    }

    public String getHighlightedText(RedDawnSession session, Sentence sentence) {
        List<OffsetItem> offsetItems = getSentenceOffsetItems(session, sentence);
        try {
            return getHighlightedText(sentence.getData().getText(), new Integer((int) (long) sentence.getData().getStart()), offsetItems);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private List<OffsetItem> getSentenceOffsetItems(RedDawnSession session, Sentence sentence) {
        List<OffsetItem> offsetItems = new ArrayList<OffsetItem>();
        List<SentenceTerm> sentenceTerms = sentence.getSentenceTerms();
        for (SentenceTerm sentenceTerm : sentenceTerms) {

            String termId = sentenceTerm.getTermId();
            String columnFamilyName = sentenceTerm.getColumnFamilyName();

            Term term = termRepository.findByRowKey(session.getModelSession(), termId);
            TermMention termMention = term.<TermMention>get(columnFamilyName);
            offsetItems.add(new TermAndTermMentionOffsetItem(new TermAndTermMention(term, termMention)));
        }
        return offsetItems;
    }
}
