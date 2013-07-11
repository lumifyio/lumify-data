package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.ucd.term.Term;
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
            List<TermAndTermMention> termAndTermMetadata = getTermAndTermMetadataForArtifact(artifact.getRowKey(), terms);
            String highlightedText = getHighlightedText(artifact.getContent().getDocExtractedTextString(), termAndTermMetadata);
            if (highlightedText == null) {
                return false;
            }
            artifact.getContent().setHighlightedText(highlightedText);
            return true;
        }

        public static List<TermAndTermMention> getTermAndTermMetadataForArtifact(ArtifactRowKey artifactKey, Collection<Term> terms) {
            ArrayList<TermAndTermMention> termMetadatas = new ArrayList<TermAndTermMention>();
            for (Term term : terms) {
                for (TermMention termMention : term.getTermMentions()) {
                    if (termMention.getArtifactKey().equals(artifactKey.toString())) {
                        termMetadatas.add(new TermAndTermMention(term, termMention));
                    }
                }
            }
            return termMetadatas;
        }

        public static String getHighlightedText(String text, List<TermAndTermMention> termAndTermMetadatas) throws JSONException {
            Collections.sort(termAndTermMetadatas, new TermAndTermMetadataComparator());
            long start = 0;
            StringBuilder result = new StringBuilder();
            for (TermAndTermMention termAndTermMetadata : termAndTermMetadatas) {
                TermMention mention = termAndTermMetadata.getTermMention();
                String keyString = termAndTermMetadata.getTerm().getRowKey().toString();
                keyString = keyString.replaceAll("\\x1f", "\\\\x1F");

                if (mention.getMentionStart() < start) {
                    continue; // TODO handle overlapping entities (see com.altamiracorp.reddawn.entityExtraction.EntityHighlightTest#testGetHighlightedTextOverlaps)
                }

                JSONObject infoJson = new JSONObject();
                infoJson.put("rowKey", keyString);
                infoJson.put("type", "entities");
                infoJson.put("subType", termAndTermMetadata.getTerm().getRowKey().getConceptLabel());

                result.append(text.substring((int) start, (int) mention.getMentionStart().longValue()));
                result.append("<span");
                result.append(" class=\"entity ");
                result.append(termAndTermMetadata.getTerm().getRowKey().getConceptLabel());
                result.append("\"");
                result.append(" data-info=\"");
                result.append(StringEscapeUtils.escapeHtml(infoJson.toString()));
                result.append("\"");
                result.append(">");
                result.append(text.substring((int) mention.getMentionStart().longValue(), (int) mention.getMentionEnd().longValue()));
                result.append("</span>");
                start = mention.getMentionEnd();
            }
            result.append(text.substring((int) start));
            return result.toString();
        }

        public static class TermAndTermMention {
            private Term term;
            private TermMention termMention;

            public TermAndTermMention(Term term, TermMention termMetadata) {
                this.term = term;
                this.termMention = termMetadata;
            }

            public Term getTerm() {
                return term;
            }

            public TermMention getTermMention() {
                return termMention;
            }

            @Override
            public String toString() {
                return getTerm().getRowKey().getSign()
                        + " - "
                        + getTermMention().getMentionStart()
                        + "-"
                        + getTermMention().getMentionEnd();
            }
        }

        public static class TermAndTermMetadataComparator implements Comparator<TermAndTermMention> {
            @Override
            public int compare(TermAndTermMention t1, TermAndTermMention t2) {
                long t1Start = t1.getTermMention().getMentionStart();
                long t2Start = t2.getTermMention().getMentionStart();
                if (t1Start == t2Start) {
                    long t1End = t1.getTermMention().getMentionEnd();
                    long t2End = t2.getTermMention().getMentionEnd();
                    if (t1End == t2End) {
                        return 0;
                    }
                    return t1End > t2End ? 1 : -1;
                }
                return t1Start > t2Start ? 1 : -1;
            }
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
}

