package com.altamiracorp.reddawn.location;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class SimpleArtifactLocationExtractor implements ArtifactLocationExtractor {
    @Override
    public void setup(Mapper.Context context) throws IOException {

    }

    @Override
    public Collection<Artifact> extract(Term term) throws Exception {
        ArrayList<Artifact> result = new ArrayList<Artifact>();

        for (TermMention termMention : term.getTermMentions()) {
            String geoLocation = termMention.getGeoLocation();
            if (geoLocation != null) {
                Artifact artifact = new Artifact(termMention.getArtifactKey());
                artifact.getDynamicMetadata().setGeolocation(geoLocation);
                result.add(artifact);
            }
        }

        return result;
    }
}
