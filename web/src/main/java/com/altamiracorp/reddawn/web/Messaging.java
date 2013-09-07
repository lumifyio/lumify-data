package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.user.User;
import com.altamiracorp.reddawn.model.user.UserRepository;
import com.altamiracorp.reddawn.model.user.UserStatus;
import org.atmosphere.cache.UUIDBroadcasterCache;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.AtmosphereHandlerService;
import org.atmosphere.cpr.*;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.BroadcastOnPostAtmosphereInterceptor;
import org.atmosphere.interceptor.HeartbeatInterceptor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(Messaging.class);
    private UserRepository userRepository = new UserRepository();
    private static RedDawnSession cachedSession;

    @Override
    public void onRequest(AtmosphereResource resource) throws IOException {
        AtmosphereRequest req = resource.getRequest();
        if (resource.getRequest().getMethod().equalsIgnoreCase("GET")) {
            onOpen(resource);
            resource.suspend();
        } else if (req.getMethod().equalsIgnoreCase("POST")) {
            resource.getBroadcaster().broadcast(req.getReader().readLine().trim());
        }
    }

    @Override
    public void destroy() {
        LOGGER.debug("destroy");
    }

    @Override
    public void onStateChange(AtmosphereResourceEvent event) throws IOException {
        AtmosphereResponse response = ((AtmosphereResourceImpl) event.getResource()).getResponse(false);

        if (event.getMessage() != null && List.class.isAssignableFrom(event.getMessage().getClass())) {
            List<String> messages = List.class.cast(event.getMessage());
            for (String t : messages) {
                onMessage(event, response, t);
            }
        } else if (event.isSuspended()) {
            onMessage(event, response, (String) event.getMessage());
        } else if (event.isResuming()) {
            onResume(event, response);
        } else if (event.isResumedOnTimeout()) {
            onTimeout(event, response);
        } else if (event.isCancelled()) {
            onDisconnect(event, response);
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
        LOGGER.debug("onDisconnect");

        setStatus(event.getResource(), UserStatus.OFFLINE);
    }

    public void onMessage(AtmosphereResourceEvent event, AtmosphereResponse response, String message) throws IOException {
        response.write(message);
    }

    private void setStatus(AtmosphereResource resource, UserStatus status) {
        RedDawnSession session = getRedDawnSession(resource);
        try {
            User user = DevBasicAuthenticator.getUser(resource.getRequest().getSession());
            user.getMetadata().setStatus(status);
            userRepository.save(session.getModelSession(), user);

            JSONObject json = new JSONObject();
            json.put("type", "userStatusChange");
            json.put("data", user.toJson());
            resource.getBroadcaster().broadcast(json.toString());
        } catch (Exception ex) {
            LOGGER.error("Could not update status", ex);
        } finally {
            // TODO session is held open by getRedDawnSession
            // session.close();
        }
    }

    private RedDawnSession getRedDawnSession(AtmosphereResource resource) {
        // TODO: should we create a new session each time?
        if (cachedSession != null) {
            return cachedSession;
        }
        RedDawnSession session = WebSessionFactory.createRedDawnSession(resource.getRequest());
        cachedSession = session;
        return session;
    }
}
