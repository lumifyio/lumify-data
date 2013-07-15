package com.altamiracorp.reddawn.web.routes.artifact;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.entityExtraction.EntityHighlightMR;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermAndTermMention;
import com.altamiracorp.reddawn.ucd.term.TermAndTermMetadataComparator;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.reddawn.web.utils.UrlUtils;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// TODO remove or disable this route once highlighting is stable
public class ArtifactHtmlByRowKey implements Handler, AppAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactHtmlByRowKey.class.getName());
    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private TermRepository termRepository = new TermRepository();
    private WebApp app;

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        LOGGER.warn("*** This route should be called with care. It is using the slow way to get the html");

        RedDawnSession session = app.getRedDawnSession(request);
        ArtifactRowKey artifactKey = new ArtifactRowKey(UrlUtils.urlDecode((String) request.getAttribute("rowKey")));
        Artifact artifact = artifactRepository.findByRowKey(session.getModelSession(), artifactKey.toString());

        Collection<Term> terms = termRepository.findByArtifactRowKey(session.getModelSession(), artifactKey.toString());
        List<TermAndTermMention> termAndTermMetadatas = EntityHighlightMR.EntityHighlightMapper.getTermAndTermMetadataForArtifact(artifactKey, terms);
        Collections.sort(termAndTermMetadatas, new TermAndTermMetadataComparator());
        for (TermAndTermMention termAndTermMetadata : termAndTermMetadatas) {
            LOGGER.info(termAndTermMetadata.toString());
        }
        String highlightedText = EntityHighlightMR.EntityHighlightMapper.getHighlightedText(artifact.getContent().getDocExtractedTextString(), termAndTermMetadatas);

        response.setContentType("text/html");
        response.getWriter().write("<html>");
        response.getWriter().write("<head>");
        response.getWriter().write("<style>");
        response.getWriter().write(".entity { background-color: #ff0000; }");
        response.getWriter().write(".entity.date { background-color: #00ff00; }");
        response.getWriter().write(".entity.time { background-color: #0000ff; }");
        response.getWriter().write(".entity.location { background-color: #ffff00; }");
        response.getWriter().write(".entity.money { background-color: #00ffff; }");
        response.getWriter().write(".entity.organization { background-color: #ff00ff; }");
        response.getWriter().write(".entity.percentage { background-color: #ff5555; }");
        response.getWriter().write(".entity.person { background-color: #55ff55; }");
        response.getWriter().write("</style>");
        response.getWriter().write("</head>");
        response.getWriter().write("<body>");
        response.getWriter().write("<pre>");
        response.getWriter().write(highlightedText);
        response.getWriter().write("</pre>");
        response.getWriter().write("</body>");
        response.getWriter().write("</html>");
    }
}
