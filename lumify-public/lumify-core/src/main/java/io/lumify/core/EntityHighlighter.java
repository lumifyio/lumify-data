package io.lumify.core;

import io.lumify.core.model.termMention.TermMentionModel;
import io.lumify.core.model.textHighlighting.OffsetItem;
import io.lumify.core.model.textHighlighting.TermMentionOffsetItem;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class EntityHighlighter {
    public String getHighlightedText(String text, Iterable<TermMentionModel> termMentions) {
        List<OffsetItem> offsetItems = convertTermMentionsToOffsetItems(termMentions);
        return getHighlightedText(text, 0, offsetItems);
    }

    // TODO: change to use an InputStream?
    public static String getHighlightedText(String text, int textStartOffset, List<OffsetItem> offsetItems) throws JSONException {
        Collections.sort(offsetItems);
        StringBuilder result = new StringBuilder();
        PriorityQueue<Integer> endOffsets = new PriorityQueue<Integer>();
        int lastStart = textStartOffset;
        for (int i = 0; i < offsetItems.size(); i++) {
            OffsetItem offsetItem = offsetItems.get(i);

            boolean overlapsPreviousItem = false;
            if (offsetItem instanceof TermMentionOffsetItem) {
                for (int j = 0; j < i; j++) {
                    OffsetItem compareItem = offsetItems.get(j);
                    if (compareItem instanceof TermMentionOffsetItem && (compareItem.getEnd() >= offsetItem.getEnd()
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
                result.append(escapeHtml(safeSubstring(text, lastStart - textStartOffset, end - textStartOffset)));
                result.append("</span>");
                lastStart = end;
            }
            result.append(escapeHtml(safeSubstring(text, lastStart - textStartOffset, (int) (offsetItem.getStart() - textStartOffset))));

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
            result.append(escapeHtml(safeSubstring(text, lastStart - textStartOffset, end - textStartOffset)));
            result.append("</span>");
            lastStart = end;
        }
        result.append(escapeHtml(safeSubstring(text, lastStart - textStartOffset)));

        return result.toString();
    }

    private static String safeSubstring(String text, int beginIndex) {
        beginIndex = Math.min(beginIndex, text.length());
        return text.substring(beginIndex);
    }

    private static String safeSubstring(String text, int beginIndex, int endIndex) {
        beginIndex = Math.min(beginIndex, text.length());
        endIndex = Math.min(endIndex, text.length());
        return text.substring(beginIndex, endIndex);
    }

    private static String escapeHtml(String text) {
        return text
                .replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }

    public List<OffsetItem> convertTermMentionsToOffsetItems(Iterable<TermMentionModel> termMentions) {
        ArrayList<OffsetItem> termMetadataOffsetItems = new ArrayList<OffsetItem>();
        for (TermMentionModel termMention : termMentions) {
            termMetadataOffsetItems.add(new TermMentionOffsetItem(termMention));
        }
        return termMetadataOffsetItems;
    }
}
