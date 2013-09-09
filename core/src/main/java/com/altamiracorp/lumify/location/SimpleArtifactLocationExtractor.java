package com.altamiracorp.lumify.location;

import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;

public class SimpleArtifactLocationExtractor implements ArtifactLocationExtractor {
    @Override
    public void setup(Mapper.Context context) throws IOException {

    }

    @Override
    public void extract(Artifact artifact, List<TermMention> termMentions) throws Exception {
        TermMention largest = null;

        for (TermMention termMention : termMentions) {
            if (termMention.getMetadata().getGeoLocation() != null) {
                if (largest == null) {
                    largest = termMention;
                    continue;
                }
                if (termMention.getMetadata().getGeoLocationPopulation() > largest.getMetadata().getGeoLocationPopulation()) {
                    largest = termMention;
                    continue;
                }
            }
        }

        if (largest != null) {
            artifact.getDynamicMetadata().setGeolocation(largest.getMetadata().getGeoLocation());
            artifact.getDynamicMetadata().setGeoLocationPopulation(largest.getMetadata().getGeoLocationPopulation());
            artifact.getDynamicMetadata().setGeoLocationTitle(largest.getMetadata().getGeoLocationTitle());
        }
    }
}
