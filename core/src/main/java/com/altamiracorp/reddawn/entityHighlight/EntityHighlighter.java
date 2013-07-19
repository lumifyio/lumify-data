package com.altamiracorp.reddawn.entityHighlight;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRepository;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermAndTermMention;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
import org.apache.commons.lang.StringEscapeUtils;
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

            return getHighlightedText(artifact.getContent().getDocExtractedTextString(), offsetItems);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getHighlightedText(String text, List<OffsetItem> offsetItems) throws JSONException {
        Collections.sort(offsetItems, new OffsetItemComparator());
        StringBuilder result = new StringBuilder();
        PriorityQueue<Integer> endOffsets = new PriorityQueue<Integer>();
        int lastStart = 0;
        for (OffsetItem offsetItem : offsetItems) {
            while (endOffsets.size() > 0 && endOffsets.peek() < offsetItem.getStart().intValue()) {
                int end = endOffsets.poll();
                result.append(text.substring(lastStart, end));
                result.append("</span>");
                lastStart = end;
            }
            result.append(text.substring(lastStart, offsetItem.getStart().intValue()));

            String rowKey = offsetItem.getRowKey();
            rowKey = rowKey.replaceAll("\\x1f", "\\\\x1F");

            JSONObject infoJson = new JSONObject();
            infoJson.put("start", offsetItem.getStart());
            infoJson.put("end", offsetItem.getEnd());
            infoJson.put("rowKey", rowKey);
            infoJson.put("type", offsetItem.getType());
            if (offsetItem.getSubType() != null) {
                infoJson.put("subType", offsetItem.getSubType());
            }
            if (offsetItem.getObjectRowKey() != null) {
                infoJson.put("objectRowKey", offsetItem.getObjectRowKey().toJson());
            }

            result.append("<span");
            result.append(" class=\"");
            result.append(offsetItem.getType());
            if (offsetItem.getConceptLabel() != null) {
                result.append(" " + offsetItem.getConceptLabel());
            }
            result.append("\"");
            result.append(" data-info=\"");
            result.append(StringEscapeUtils.escapeHtml(infoJson.toString()));
            result.append("\"");
            result.append(">");
            endOffsets.add(offsetItem.getEnd().intValue());
            lastStart = offsetItem.getStart().intValue();
        }
        result.append(text.substring(lastStart));
        return result.toString();
    }

    private List<OffsetItem> getSentencesForArtifact(RedDawnSession session, ArtifactRowKey rowKey) {
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
                    termMetadataOffsetItems.add(new TermAndTermMetadataOffsetItem(new TermAndTermMention(term, termMention)));
                }
            }
        }
        return termMetadataOffsetItems;
    }
}
