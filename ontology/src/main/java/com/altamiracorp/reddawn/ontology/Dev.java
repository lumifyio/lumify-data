package com.altamiracorp.reddawn.ontology;

import com.thinkaurelius.titan.core.TitanGraph;

public class Dev extends Base {
    public static void main(String[] args) throws Exception {
        new Dev().run(args);
    }

    @Override
    protected int defineOntology(TitanGraph graph) {
        return 0;
    }
}
