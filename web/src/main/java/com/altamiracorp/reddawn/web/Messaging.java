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

    @Override
    public void onRequest(AtmosphereResource resource) throws IOException {
        AtmosphereRequest req = resource.getRequest();
        if (resource.getRequest().getMethod().equalsIgnoreCase("GET")) {
            LOGGER.info("onRequest GET");
            onOpen(resource);
            resource.suspend();
        } else if (req.getMethod().equalsIgnoreCase("POST")) {
            LOGGER.info("onRequest POST");
            resource.getBroadcaster().broadcast(req.getReader().readLine().trim());
        }
    }

    @Override
    public void destroy() {
        LOGGER.info("destroy");
    }

    @Override
    public void onStateChange(AtmosphereResourceEvent event) throws IOException {
        AtmosphereResponse response = ((AtmosphereResourceImpl) event.getResource()).getResponse(false);

        LOGGER.info("{} with event {}", event.getResource().uuid(), event);
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
        LOGGER.error("onResume");
    }

    public void onTimeout(AtmosphereResourceEvent event, AtmosphereResponse response) throws IOException {
        LOGGER.error("onTimeout");
    }

    public void onDisconnect(AtmosphereResourceEvent event, AtmosphereResponse response) throws IOException {
        LOGGER.error("onDisconnect");

        setStatus(event.getResource(), UserStatus.OFFLINE);
    }

    public void onMessage(AtmosphereResourceEvent event, AtmosphereResponse response, String message) throws IOException {
        response.write(message);
    }

    private void setStatus(AtmosphereResource resource, UserStatus status) {
        RedDawnSession session = WebSessionFactory.createRedDawnSession(resource.getRequest());
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
            session.close();
        }
    }
}
