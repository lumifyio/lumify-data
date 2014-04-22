package com.altamiracorp.lumify.web;

import com.altamiracorp.lumify.core.model.user.UserRepository;
import com.altamiracorp.lumify.core.model.user.UserStatus;
import com.altamiracorp.lumify.core.model.workQueue.WorkQueueRepository;
import com.altamiracorp.lumify.core.model.workspace.Workspace;
import com.altamiracorp.lumify.core.model.workspace.WorkspaceRepository;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.commons.lang.StringUtils;
import org.atmosphere.cache.UUIDBroadcasterCache;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.AtmosphereHandlerService;
import org.atmosphere.cpr.*;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.BroadcastOnPostAtmosphereInterceptor;
import org.atmosphere.interceptor.HeartbeatInterceptor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

@AtmosphereHandlerService(
        path = "/messaging",
        broadcasterCache = UUIDBroadcasterCache.class,
        interceptors = {
                AtmosphereResourceLifecycleInterceptor.class,
                BroadcastOnPostAtmosphereInterceptor.class,
                TrackMessageSizeInterceptor.class,
                HeartbeatInterceptor.class
        })
public class Messaging implements AtmosphereHandler { //extends AbstractReflectorAtmosphereHandler {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(Messaging.class);

    private UserRepository userRepository;

    // TODO should we save off this broadcaster? When using the BroadcasterFactory
    //      we always get null when trying to get the default broadcaster
    private static Broadcaster broadcaster;
    private WorkspaceRepository workspaceRepository;
    private WorkQueueRepository workQueueRepository;
    private boolean subscribedToBroadcast = false;

    @Override
    public void onRequest(AtmosphereResource resource) throws IOException {
        ensureInitialized(resource);

        String requestData = org.apache.commons.io.IOUtils.toString(resource.getRequest().getInputStream());
        try {
            if (!StringUtils.isBlank(requestData)) {
                processRequestData(resource, requestData);
            }
        } catch (Exception ex) {
            LOGGER.error("Could not handle async message: " + requestData, ex);
        }

        AtmosphereRequest req = resource.getRequest();
        if (req.getMethod().equalsIgnoreCase("GET")) {
            onOpen(resource);
            resource.suspend();
        } else if (req.getMethod().equalsIgnoreCase("POST")) {
            resource.getBroadcaster().broadcast(req.getReader().readLine().trim());
        }
    }

    private void ensureInitialized(AtmosphereResource resource) {
        if (userRepository == null) {
            Injector injector = (Injector) resource.getAtmosphereConfig().getServletContext().getAttribute(Injector.class.getName());
            injector.injectMembers(this);
        }
        if (!subscribedToBroadcast) {
            this.workQueueRepository.subscribeToBroadcastMessages(new WorkQueueRepository.BroadcastConsumer() {
                @Override
                public void broadcastReceived(JSONObject json) {
                    if (broadcaster != null) {
                        broadcaster.broadcast(json.toString());
                    }
                }
            });
            subscribedToBroadcast = true;
        }
        broadcaster = resource.getBroadcaster();
    }

    @Override
    public void destroy() {
        LOGGER.debug("destroy");
    }

    @Override
    public void onStateChange(AtmosphereResourceEvent event) throws IOException {
        ensureInitialized(event.getResource());
        AtmosphereResponse response = ((AtmosphereResourceImpl) event.getResource()).getResponse(false);

        if (event.getMessage() != null && List.class.isAssignableFrom(event.getMessage().getClass())) {
            List<String> messages = List.class.cast(event.getMessage());
            for (String t : messages) {
                onMessage(event, response, t);
            }

        } else if (event.isClosedByApplication() || event.isClosedByClient() || event.isCancelled()) {
            onDisconnect(event, response);
        } else if (event.isSuspended()) {
            onMessage(event, response, (String) event.getMessage());
        } else if (event.isResuming()) {
            onResume(event, response);
        } else if (event.isResumedOnTimeout()) {
            onTimeout(event, response);
        }
    }

    public void onOpen(AtmosphereResource resource) throws IOException {
        setStatus(resource, UserStatus.ONLINE);
    }

    public void onResume(AtmosphereResourceEvent event, AtmosphereResponse response) throws IOException {
        LOGGER.debug("onResume");
    }

    public void onTimeout(AtmosphereResourceEvent event, AtmosphereResponse response) throws IOException {
        LOGGER.debug("onTimeout");
    }

    public void onDisconnect(AtmosphereResourceEvent event, AtmosphereResponse response) throws IOException {
        setStatus(event.getResource(), UserStatus.OFFLINE);
    }

    public void onClose(AtmosphereResourceEvent event, AtmosphereResponse response) {
        setStatus(event.getResource(), UserStatus.OFFLINE);
    }

    public void onMessage(AtmosphereResourceEvent event, AtmosphereResponse response, String message) throws IOException {
        try {
            if (!StringUtils.isBlank(message)) {
                processRequestData(event.getResource(), message);
            }
        } catch (Exception ex) {
            LOGGER.error("Could not handle async message: " + message, ex);
        }
        response.write(message);
    }

    private void processRequestData(AtmosphereResource resource, String message) {
        JSONObject messageJson = new JSONObject(message);

        String type = messageJson.optString("type");
        if (type == null) {
            return;
        }

        JSONObject dataJson = messageJson.optJSONObject("data");
        if (dataJson == null) {
            return;
        }

        if ("changedWorkspace".equals(type)) {
            com.altamiracorp.lumify.core.user.User authUser = AuthenticationProvider.getUser(resource.session());
            if (authUser == null) {
                throw new RuntimeException("Could not find user in session");
            }
            String workspaceId = dataJson.getString("workspaceId");
            String userId = dataJson.getString("userId");
            if (userId.equals(authUser.getUserId())) {
                switchWorkspace(authUser, workspaceId);
            }
        }
    }

    private void switchWorkspace(com.altamiracorp.lumify.core.user.User authUser, String workspaceId) {
        if (!workspaceId.equals(userRepository.getCurrentWorkspaceId(authUser.getUserId()))) {
            Workspace workspace = workspaceRepository.findById(workspaceId, authUser);
            // TODO can setCurrentWorkspace just take the workspaceId?
            userRepository.setCurrentWorkspace(authUser.getUserId(), workspace);

            LOGGER.debug("User %s switched current workspace to %s", authUser.getUserId(), workspaceId);
        }
    }

    private void setStatus(AtmosphereResource resource, UserStatus status) {
        broadcaster = resource.getBroadcaster();
        try {
            com.altamiracorp.lumify.core.user.User authUser = AuthenticationProvider.getUser(resource.getRequest().getSession());
            if (authUser == null) {
                throw new RuntimeException("Could not find user in session");
            }
            com.altamiracorp.lumify.core.user.User user = userRepository.setStatus(authUser.getUserId(), status);

            JSONObject json = new JSONObject();
            json.put("type", "userStatusChange");
            json.put("data", userRepository.toJson(user));
            resource.getBroadcaster().broadcast(json.toString());
        } catch (Exception ex) {
            LOGGER.error("Could not update status", ex);
        } finally {
            // TODO session is held open by getAppSession
            // session.close();
        }
    }

    public static void broadcastEdgeDeletion(String edgeId) {
        try {
            JSONObject dataJson = new JSONObject();
            if (edgeId != null) {
                dataJson.put("edgeId", edgeId);
            }

            JSONObject json = new JSONObject();
            json.put("type", "edgeDeletion");
            json.put("data", dataJson);
            if (broadcaster != null) {
                broadcaster.broadcast(json.toString());
            }
        } catch (JSONException ex) {
            throw new RuntimeException("Could not create json", ex);
        }
    }

    public static void broadcastDetectedObjectChange(String artifactVertexId, JSONObject artifactVertexWithDetectedObjects) {
        try {
            JSONObject dataJson = new JSONObject();
            if (artifactVertexWithDetectedObjects != null) {
                dataJson = artifactVertexWithDetectedObjects;
            }

            JSONObject json = new JSONObject();
            json.put("type", "detectedObjectChange");
            json.put("data", dataJson);
            if (broadcaster != null) {
                broadcaster.broadcast(json.toString());
            }
        } catch (JSONException ex) {
            throw new RuntimeException("Could not create json", ex);
        }
    }

    @Inject
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Inject
    public void setWorkspaceRepository(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    @Inject
    public void setWorkQueueRepository(WorkQueueRepository workQueueRepository) {
        this.workQueueRepository = workQueueRepository;
    }
}
