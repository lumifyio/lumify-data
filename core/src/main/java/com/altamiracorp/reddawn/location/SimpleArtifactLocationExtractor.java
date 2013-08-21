package com.altamiracorp.reddawn.location;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.term.TermAndTermMention;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;

public class SimpleArtifactLocationExtractor implements ArtifactLocationExtractor {
    @Override
    public void setup(Mapper.Context context) throws IOException {

    }

    @Override
    public void extract(Artifact artifact, List<TermAndTermMention> termAndTermMentions) throws Exception {
        TermAndTermMention largest = null;

        for (TermAndTermMention termAndTermMention : termAndTermMentions) {
            if (termAndTermMention.getTermMention().getGeoLocation() != null) {
                if (largest == null) {
                    largest = termAndTermMention;
                    continue;
                }
                if (termAndTermMention.getTermMention().getGeoLocationPopulation() > largest.getTermMention().getGeoLocationPopulation()) {
                    largest = termAndTermMention;
                    continue;
                }
            }
        }

        if (largest != null) {
            artifact.getDynamicMetadata().setGeolocation(largest.getTermMention().getGeoLocation());
            artifact.getDynamicMetadata().setGeoLocationPopulation(largest.getTermMention().getGeoLocationPopulation());
            artifact.getDynamicMetadata().setGeoLocationTitle(largest.getTermMention().getGeoLocationTitle());
        }
    }
}
