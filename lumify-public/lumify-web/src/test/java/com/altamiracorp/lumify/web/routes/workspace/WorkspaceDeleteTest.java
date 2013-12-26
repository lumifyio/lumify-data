package com.altamiracorp.lumify.web.routes.workspace;

import com.altamiracorp.bigtable.model.user.ModelUserContext;
import com.altamiracorp.lumify.core.model.workspace.WorkspaceRepository;
import com.altamiracorp.lumify.core.model.workspace.WorkspaceRowKey;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.web.AuthenticationProvider;
import com.altamiracorp.lumify.web.routes.RouteTestBase;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class WorkspaceDeleteTest extends RouteTestBase {
    private WorkspaceDelete workspaceDelete;

    @Mock
    private WorkspaceRepository mockWorkspaceRepository;
    @Mock
    private HttpSession mockHttpSession;
    @Mock
    private User mockUser;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        workspaceDelete = new WorkspaceDelete(mockWorkspaceRepository);
    }

    @Test
    public void testHandle() throws Exception {
        when(mockRequest.getAttribute("workspaceRowKey")).thenReturn("workspaceRowKey");
        when(mockRequest.getSession()).thenReturn(mockHttpSession);
        when(AuthenticationProvider.getUser(mockHttpSession)).thenReturn(mockUser);

        workspaceDelete.handle(mockRequest, mockResponse, mockHandlerChain);
        verify(mockWorkspaceRepository, times(1)).delete(any(WorkspaceRowKey.class), any(ModelUserContext.class));
        JSONObject response = new JSONObject(responseStringWriter.getBuffer().toString());
        assertTrue(response.getBoolean("success"));
    }
}
