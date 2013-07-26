package com.altamiracorp.reddawn.web.routes;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.altamiracorp.reddawn.ucd.object.UcdObjectRepository;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRepository;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRepository;
import com.altamiracorp.reddawn.ucd.statement.StatementRepository;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.HandlerChain;
import org.mockito.Mockito;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.when;

public abstract class RouteTestBase {
    public HttpServletRequest mockRequest;
    public HttpServletResponse mockResponse;
    public HandlerChain mockHandlerChain;
    public WebApp mockApp;
    public RedDawnSession mockRedDawnSessionSession;
    public Session mockModelSession;
    public StringWriter responseStringWriter;
    public ServletOutputStream mockResponseOutputStream;

    public PredicateRepository mockPredicateRepository;
    public ArtifactRepository mockArtifactRepository;
    public TermRepository mockTermRepository;
    public StatementRepository mockStatementRepository;
    public SentenceRepository mockSentenceRepository;
    public UcdObjectRepository mockUcdObjectRepository;

    public void setUp() throws Exception {
        responseStringWriter = new StringWriter();
        mockResponseOutputStream = Mockito.mock(ServletOutputStream.class);

        mockApp = Mockito.mock(WebApp.class);
        mockRequest = Mockito.mock(HttpServletRequest.class);
        mockResponse = Mockito.mock(HttpServletResponse.class);
        mockHandlerChain = Mockito.mock(HandlerChain.class);
        mockRedDawnSessionSession = Mockito.mock(RedDawnSession.class);
        mockModelSession = Mockito.mock(Session.class);

        mockPredicateRepository = Mockito.mock(PredicateRepository.class);
        mockArtifactRepository = Mockito.mock(ArtifactRepository.class);
        mockTermRepository = Mockito.mock(TermRepository.class);
        mockStatementRepository = Mockito.mock(StatementRepository.class);
        mockSentenceRepository = Mockito.mock(SentenceRepository.class);
        mockUcdObjectRepository = Mockito.mock(UcdObjectRepository.class);

        //request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
        when(mockRequest.getScheme()).thenReturn("http");
        when(mockRequest.getServerName()).thenReturn("testServerName");
        when(mockRequest.getServerPort()).thenReturn(80);

        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseStringWriter));
        when(mockResponse.getOutputStream()).thenReturn(mockResponseOutputStream);
        when(mockApp.getRedDawnSession(mockRequest)).thenReturn(mockRedDawnSessionSession);
        when(mockRedDawnSessionSession.getModelSession()).thenReturn(mockModelSession);
    }
}
