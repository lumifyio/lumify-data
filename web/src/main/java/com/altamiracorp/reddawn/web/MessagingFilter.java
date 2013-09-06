package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.model.user.User;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.PerRequestBroadcastFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagingFilter implements PerRequestBroadcastFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingFilter.class);

    @Override
    public BroadcastAction filter(AtmosphereResource r, Object originalMessage, Object message) {
        try {
            JSONObject json = new JSONObject("" + originalMessage);
            JSONObject permissionsJson = json.optJSONObject("permissions");

            JSONArray users = permissionsJson.optJSONArray("users");
            if (users != null) {
                User currentUser = DevBasicAuthenticator.getUser(r.getRequest().getSession());
                if (!isUserInList(users, currentUser)) {
                    return new BroadcastAction(BroadcastAction.ACTION.ABORT, message);
                }
            }

            JSONArray workspaces = permissionsJson.optJSONArray("workspaces");
            if (workspaces != null) {
                User currentUser = DevBasicAuthenticator.getUser(r.getRequest().getSession());
                if (!isWorkspaceInList(workspaces, currentUser.getMetadata().getCurrentWorkspace())) {
                    return new BroadcastAction(BroadcastAction.ACTION.ABORT, message);
                }
            }

            return new BroadcastAction(message);
        } catch (JSONException e) {
            LOGGER.error("Failed to filter message:\n" + originalMessage, e);
            return new BroadcastAction(BroadcastAction.ACTION.ABORT, message);
        }
    }

    private boolean isWorkspaceInList(JSONArray workspaces, String currentWorkspace) throws JSONException {
        for (int i = 0; i < workspaces.length(); i++) {
            String workspaceItemRowKey = workspaces.getString(i);
            if (workspaceItemRowKey.equals(currentWorkspace)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUserInList(JSONArray users, User user) throws JSONException {
        String userRowKey = user.getRowKey().toString();
        for (int i = 0; i < users.length(); i++) {
            String userItemRowKey = users.getString(i);
            if (userItemRowKey.equals(userRowKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BroadcastAction filter(Object originalMessage, Object message) {
        return new BroadcastAction(message);
    }
}
