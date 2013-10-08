package com.altamiracorp.lumify.web;

import com.altamiracorp.lumify.web.routes.admin.*;
import com.altamiracorp.lumify.web.routes.artifact.*;
import com.altamiracorp.lumify.web.routes.entity.*;
import com.altamiracorp.lumify.web.routes.graph.*;
import com.altamiracorp.lumify.web.routes.map.MapInitHandler;
import com.altamiracorp.lumify.web.routes.map.MapMarkerImage;
import com.altamiracorp.lumify.web.routes.map.MapTileHandler;
import com.altamiracorp.lumify.web.routes.ontology.*;
import com.altamiracorp.lumify.web.routes.relationship.RelationshipCreate;
import com.altamiracorp.lumify.web.routes.relationship.SetRelationshipProperty;
import com.altamiracorp.lumify.web.routes.resource.ResourceGet;
import com.altamiracorp.lumify.web.routes.user.MeGet;
import com.altamiracorp.lumify.web.routes.user.UserList;
import com.altamiracorp.lumify.web.routes.vertex.*;
import com.altamiracorp.lumify.web.routes.workspace.*;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.StaticFileHandler;
import com.google.inject.Injector;
import org.eclipse.jetty.server.Request;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class Router extends HttpServlet {
    private static final MultipartConfigElement MULTI_PART_CONFIG = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));
    private WebApp app;
    final File rootDir = new File("./web/src/main/webapp");

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        final Injector injector = (Injector) config.getServletContext().getAttribute(Injector.class.getName());

        app = new WebApp(config, injector);

        AuthenticationProvider authenticatorInstance = injector.getInstance(AuthenticationProvider.class);
        Class<? extends Handler> authenticator = authenticatorInstance.getClass();

        app.get("/index.html", authenticatorInstance, new StaticFileHandler(config));

        app.get("/ontology/concept/{conceptId}/properties", authenticator, PropertyListByConceptId.class);
        app.get("/ontology/{relationshipLabel}/properties", authenticator, PropertyListByRelationshipLabel.class);
        app.get("/ontology/concept/", authenticator, ConceptList.class);
        app.get("/ontology/property/", authenticator, PropertyList.class);
        app.get("/ontology/relationship/", authenticator, RelationshipLabelList.class);

        app.get("/resource/{_rowKey}", authenticator, ResourceGet.class);

        app.get("/artifact/search", authenticator, ArtifactSearch.class);
        app.get("/artifact/{_rowKey}/raw", authenticator, ArtifactRawByRowKey.class);
        app.get("/artifact/{_rowKey}/thumbnail", authenticator, ArtifactThumbnailByRowKey.class);
        app.get("/artifact/{_rowKey}/poster-frame", authenticator, ArtifactPosterFrameByRowKey.class);
        app.get("/artifact/{_rowKey}/video-preview", authenticator, ArtifactVideoPreviewImageByRowKey.class);
        app.get("/artifact/{_rowKey}", authenticator, ArtifactByRowKey.class);
        app.post("/artifact/import", authenticator, ArtifactImport.class);

        app.post("/entity/relationships", authenticator, EntityRelationships.class);
        app.post("/entity/createTerm", authenticator, EntityTermCreate.class);
        app.post("/entity/updateTerm", authenticator, EntityTermUpdate.class);
        app.post("/entity/createResolvedDetectedObject", authenticator, EntityObjectDetectionCreate.class);
        app.post("/entity/updateResolvedDetectedObject", authenticator, EntityObjectDetectionUpdate.class);
        app.post("/entity/deleteResolvedDetectedObject", authenticator, EntityObjectDetectionDelete.class);

        app.post("/vertex/{graphVertexId}/property/set", authenticator, VertexSetProperty.class);
        app.get("/vertex/{graphVertexId}/properties", authenticator, VertexProperties.class);
        app.get("/vertex/{graphVertexId}/relationships", authenticator, VertexRelationships.class);
        app.get("/vertex/relationship", authenticator, VertexToVertexRelationship.class);
        app.post("/vertex/removeRelationship", authenticator, VertexRelationshipRemoval.class);
        app.get("/vertex/multiple", authenticator, VertexMultiple.class);

        app.post("/relationship/property/set", authenticator, SetRelationshipProperty.class);
        app.post("/relationship/create", authenticator, RelationshipCreate.class);

        app.get("/graph/findPath", authenticator, GraphFindPath.class);
        app.get("/graph/{graphVertexId}/relatedVertices", authenticator, GraphRelatedVertices.class);
        app.get("/graph/vertex/search", authenticator, GraphVertexSearch.class);
        app.get("/graph/vertex/geoLocationSearch", authenticator, GraphGeoLocationSearch.class);
        app.post("/graph/vertex/{graphVertexId}/uploadImage", authenticator, GraphVertexUploadImage.class);

        app.get("/workspace/", authenticator, WorkspaceList.class);
        app.post("/workspace/save", authenticator, WorkspaceSave.class);
        app.post("/workspace/{workspaceRowKey}/copy", authenticator, WorkspaceCopy.class);
        app.post("/workspace/{workspaceRowKey}/save", authenticator, WorkspaceSave.class);
        app.get("/workspace/{workspaceRowKey}", authenticator, WorkspaceByRowKey.class);
        app.delete("/workspace/{workspaceRowKey}", authenticator, WorkspaceDelete.class);

        //app.get("/user/messages", authenticator, MessagesGet.class);
        app.get("/user/me", authenticator, MeGet.class);
        app.get("/user/", authenticator, UserList.class);

        app.get("/map/map-init.js", MapInitHandler.class);
        app.get("/map/marker/{type}/image", MapMarkerImage.class);
        app.get("/map/{z}/{x}/{y}.png", MapTileHandler.class);

        app.get("/admin/query", authenticator, AdminQuery.class);
        app.get("/admin/tables", authenticator, AdminTables.class);
        app.post("/admin/uploadOntology", authenticator, AdminUploadOntology.class);
        app.get("/admin/dictionary",authenticator, AdminDictionary.class);
        app.get("/admin/dictionary/{concept}",authenticator,AdminDictionaryByConcept.class);
        app.post("/admin/dictionary",authenticator,AdminDictionaryEntryAdd.class);
        app.delete("/admin/dictionary/{entryRowKey}",authenticator, AdminDictionaryEntryDelete.class);

        LessRestlet.init(rootDir);
        app.get("/css/{file}.css", LessRestlet.class);
    }

    @Override
    public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
        try {
            if (req.getContentType() != null && req.getContentType().startsWith("multipart/form-data")) {
                req.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, MULTI_PART_CONFIG);
            }

            HttpServletResponse httpResponse = (HttpServletResponse) resp;
            httpResponse.addHeader("Accept-Ranges", "bytes");
            app.handle((HttpServletRequest) req, httpResponse);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
