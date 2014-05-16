package io.lumify.core.model.workspace;

import io.lumify.core.model.properties.types.BooleanLumifyProperty;
import io.lumify.core.model.properties.types.IntegerLumifyProperty;
import io.lumify.core.model.properties.types.StringLumifyProperty;
import org.securegraph.TextIndexHint;

public class WorkspaceLumifyProperties {
    public static final StringLumifyProperty TITLE = new StringLumifyProperty(WorkspaceRepository.WORKSPACE_CONCEPT_NAME + "/title");
    public static final BooleanLumifyProperty WORKSPACE_TO_USER_IS_CREATOR = new BooleanLumifyProperty(WorkspaceRepository.WORKSPACE_TO_USER_RELATIONSHIP_NAME + "/creator");
    public static final StringLumifyProperty WORKSPACE_TO_USER_ACCESS = new StringLumifyProperty(WorkspaceRepository.WORKSPACE_TO_USER_RELATIONSHIP_NAME + "/access");
    public static final IntegerLumifyProperty WORKSPACE_TO_ENTITY_GRAPH_POSITION_X = new IntegerLumifyProperty(WorkspaceRepository.WORKSPACE_TO_ENTITY_RELATIONSHIP_NAME + "/graphPositionX");
    public static final IntegerLumifyProperty WORKSPACE_TO_ENTITY_GRAPH_POSITION_Y = new IntegerLumifyProperty(WorkspaceRepository.WORKSPACE_TO_ENTITY_RELATIONSHIP_NAME + "/graphPositionY");
    public static final BooleanLumifyProperty WORKSPACE_TO_ENTITY_VISIBLE = new BooleanLumifyProperty(WorkspaceRepository.WORKSPACE_TO_ENTITY_RELATIONSHIP_NAME + "/visible");
}
