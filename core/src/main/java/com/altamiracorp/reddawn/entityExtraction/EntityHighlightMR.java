package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRepository;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermAndTermMention;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class EntityHighlightMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityHighlightMR.class.getName());

    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        return EntityHighlightMapper.class;
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloArtifactInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloArtifactInputFormat.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class EntityHighlightMapper extends Mapper<Text, Artifact, Text, Artifact> {
        private TermRepository termRepository = new TermRepository();
        private SentenceRepository sentenceRepository = new SentenceRepository();
        private RedDawnSession session;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            session = ConfigurableMapJobBase.createRedDawnSession(context);
        }

        public void map(Text rowKey, Artifact artifact, Context context) throws IOException, InterruptedException {
            byte[] docExtractedText = artifact.getContent().getDocExtractedText();
            if (docExtractedText == null || docExtractedText.length < 1) {
                return;
            }

            try {
                LOGGER.info("Creating highlight text for: " + artifact.getRowKey().toString());
                Collection<Term> terms = termRepository.findByArtifactRowKey(session.getModelSession(), artifact.getRowKey().toString());
                if (populateHighlightedText(artifact, terms)) {
                    context.write(new Text(Artifact.TABLE_NAME), artifact);
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        private boolean populateHighlightedText(Artifact artifact, Collection<Term> terms) throws JSONException {
            List<OffsetItem> termAndTermMetadata = getTermAndTermMetadataForArtifact(artifact.getRowKey(), terms);
            List<OffsetItem> sentences = getSentencesForArtifact(artifact.getRowKey());
            ArrayList<OffsetItem> offsetItems = new ArrayList<OffsetItem>();
            offsetItems.addAll(termAndTermMetadata);
            offsetItems.addAll(sentences);
            String highlightedText = getHighlightedText(artifact.getContent().getDocExtractedTextString(), offsetItems);
            artifact.getContent().setHighlightedText(highlightedText);
            return true;
        }

        private List<OffsetItem> getSentencesForArtifact(ArtifactRowKey rowKey) {
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

    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new EntityHighlightMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }

    public static abstract class OffsetItem {
        public abstract Long getStart();

        public abstract Long getEnd();

        public abstract String getType();

        public abstract String getSubType();

        public abstract String getRowKey();

        public abstract String getConceptLabel();
    }

    private static class TermAndTermMetadataOffsetItem extends OffsetItem {

        private TermAndTermMention termAndTermMetadata;

        public TermAndTermMetadataOffsetItem(TermAndTermMention termAndTermMetadata) {
            this.termAndTermMetadata = termAndTermMetadata;
        }

        @Override
        public Long getStart() {
            return termAndTermMetadata.getTermMention().getMentionStart();
        }

        @Override
        public Long getEnd() {
            return termAndTermMetadata.getTermMention().getMentionEnd();
        }

        @Override
        public String getType() {
            return "entity";
        }

        @Override
        public String getSubType() {
            return termAndTermMetadata.getTerm().getRowKey().getConceptLabel();
        }

        @Override
        public String getRowKey() {
            return termAndTermMetadata.getTerm().getRowKey().toString();
        }

        @Override
        public String getConceptLabel() {
            return termAndTermMetadata.getTerm().getRowKey().getConceptLabel();
        }
    }

    private static class SentenceOffsetItem extends OffsetItem {

        private final Sentence sentence;

        public SentenceOffsetItem(Sentence sentence) {
            this.sentence = sentence;
        }

        @Override
        public Long getStart() {
            return sentence.getRowKey().getStartOffset();
        }

        @Override
        public Long getEnd() {
            return sentence.getRowKey().getEndOffset();
        }

        @Override
        public String getType() {
            return "sentence";
        }

        @Override
        public String getSubType() {
            return null;
        }

        @Override
        public String getRowKey() {
            return sentence.getRowKey().toString();
        }

        @Override
        public String getConceptLabel() {
            return null;
        }
    }

    private static class OffsetItemComparator implements Comparator<OffsetItem> {
        @Override
        public int compare(OffsetItem o1, OffsetItem o2) {
            if (o1.getStart() == o2.getStart()) {
                return Long.compare(o1.getEnd(), o2.getEnd());
            }
            return Long.compare(o1.getStart(), o2.getStart());
        }
    }
}

