package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.QueryUser;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.model.Artifact;
import com.altamiracorp.reddawn.ucd.model.ArtifactContent;
import com.altamiracorp.reddawn.ucd.model.Term;
import com.altamiracorp.reddawn.ucd.model.artifact.ArtifactKey;
import com.altamiracorp.reddawn.ucd.model.terms.TermMention;
import com.altamiracorp.reddawn.ucd.model.terms.TermMetadata;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.ToolRunner;
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

    public static class EntityHighlightMapper extends Mapper<Text, Artifact, Text, Mutation> {
        private UcdClient<AuthorizationLabel> ucdClient;
        private QueryUser<AuthorizationLabel> queryUser;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);

            try {
                ucdClient = ConfigurableMapJobBase.createUcdClient(context);
                queryUser = ConfigurableMapJobBase.getQueryUser(context);
            } catch (AccumuloSecurityException e) {
                throw new IOException(e);
            } catch (AccumuloException e) {
                throw new IOException(e);
            }
        }

        public void map(Text rowKey, Artifact artifact, Context context) throws IOException, InterruptedException {
            try {
                LOGGER.info("Creating highlight text for: " + artifact.getKey().toString());
                Collection<Term> terms = ucdClient.queryTermByArtifactKey(artifact.getKey(), queryUser);
                Mutation highlightMutation = getHighlightedTextMutation(artifact, terms);
                if (highlightMutation != null) {
                    context.write(new Text(Artifact.TABLE_NAME), highlightMutation);
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        private Mutation getHighlightedTextMutation(Artifact artifact, Collection<Term> terms) {
            List<TermAndTermMetadata> termAndTermMetadata = getTermAndTermMetadataForArtifact(artifact.getKey(), terms);
            String highlightedText = getHighlightedText(artifact.getContent().getDocExtractedText(), termAndTermMetadata);
            if (highlightedText == null) {
                return null;
            }
            Mutation mutation = new Mutation(artifact.getKey().toString());
            mutation.put(ArtifactContent.COLUMN_FAMILY_NAME, ArtifactContent.COLUMN_DOC_HIGHLIGHTED_TEXT, highlightedText);
            return mutation;
        }

        public static List<TermAndTermMetadata> getTermAndTermMetadataForArtifact(ArtifactKey artifactKey, Collection<Term> terms) {
            ArrayList<TermAndTermMetadata> termMetadatas = new ArrayList<TermAndTermMetadata>();
            for (Term term : terms) {
                for (TermMetadata termMetadata : term.getMetadata()) {
                    if (termMetadata.getArtifactKey().equals(artifactKey)) {
                        termMetadatas.add(new TermAndTermMetadata(term, termMetadata));
                    }
                }
            }
            return termMetadatas;
        }

        public static String getHighlightedText(String text, List<TermAndTermMetadata> termAndTermMetadatas) {
            Collections.sort(termAndTermMetadatas, new TermAndTermMetadataComparator());
            int start = 0;
            StringBuilder result = new StringBuilder();
            for (TermAndTermMetadata termAndTermMetadata : termAndTermMetadatas) {
                TermMention mention = termAndTermMetadata.getTermMetadata().getMention();
                String keyString = termAndTermMetadata.getTerm().getKey().toString();
                keyString = keyString.replaceAll("\\x1f", "\\\\x1F");

                if (mention.getStart() < start) {
                    continue; // TODO handle overlapping entities (see com.altamiracorp.reddawn.entityExtraction.EntityHighlightTest#testGetHighlightedTextOverlaps)
                }
                result.append(text.substring(start, mention.getStart()));
                result.append("<span");
                result.append(" class=\"entity ");
                result.append(termAndTermMetadata.getTerm().getKey().getConcept());
                result.append("\"");
                result.append(" term-key=\"");
                result.append(keyString);
                result.append("\"");
                result.append(">");
                result.append(text.substring(mention.getStart(), mention.getEnd()));
                result.append("</span>");
                start = mention.getEnd();
            }
            result.append(text.substring(start));
            return result.toString();
        }

        public static class TermAndTermMetadata {
            private Term term;
            private TermMetadata termMetadata;

            public TermAndTermMetadata(Term term, TermMetadata termMetadata) {
                this.term = term;
                this.termMetadata = termMetadata;
            }

            public Term getTerm() {
                return term;
            }

            public TermMetadata getTermMetadata() {
                return termMetadata;
            }

            @Override
            public String toString() {
                return getTerm().getKey().getSign()
                        + " - "
                        + getTermMetadata().getMention().getStart()
                        + "-"
                        + getTermMetadata().getMention().getEnd();
            }
        }

        public static class TermAndTermMetadataComparator implements Comparator<TermAndTermMetadata> {
            @Override
            public int compare(TermAndTermMetadata t1, TermAndTermMetadata t2) {
                int t1Start = t1.getTermMetadata().getMention().getStart();
                int t2Start = t2.getTermMetadata().getMention().getStart();
                if (t1Start == t2Start) {
                    int t1End = t1.getTermMetadata().getMention().getEnd();
                    int t2End = t2.getTermMetadata().getMention().getEnd();
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

