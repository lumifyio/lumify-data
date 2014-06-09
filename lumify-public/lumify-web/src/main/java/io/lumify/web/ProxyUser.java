package io.lumify.web;

import com.altamiracorp.bigtable.model.user.ModelUserContext;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.model.user.UserType;
import io.lumify.core.user.User;
import org.json.JSONObject;

import java.util.Date;

public class ProxyUser implements User {
    private final String userId;
    private final UserRepository userRepository;
    private User proxiedUser;

    public ProxyUser(String userId, UserRepository userRepository) {
        this.userId = userId;
        this.userRepository = userRepository;
    }

    @Override
    public String getUserId() { return userId; }

    @Override
    public ModelUserContext getModelUserContext() {
        ensureUser();
        if (proxiedUser == null) {
            return null;
        }
        return proxiedUser.getModelUserContext();
    }

    @Override
    public String getUsername() {
        ensureUser();
        if (proxiedUser == null) {
            return null;
        }
        return proxiedUser.getUsername();
    }

    @Override
    public String getDisplayName() {
        ensureUser();
        if (proxiedUser == null) {
            return null;
        }
        return proxiedUser.getDisplayName();
    }

    @Override
    public String getEmailAddress() {
        ensureUser();
        if (proxiedUser == null) {
            return null;
        }
        return proxiedUser.getEmailAddress();
    }

    @Override
    public Date getCreateDate() {
        ensureUser();
        if (proxiedUser == null) {
            return null;
        }
        return proxiedUser.getCreateDate();
    }

    @Override
    public Date getCurrentLoginDate() {
        ensureUser();
        if (proxiedUser == null) {
            return null;
        }
        return proxiedUser.getCurrentLoginDate();
    }

    @Override
    public String getCurrentLoginRemoteAddr() {
        ensureUser();
        if (proxiedUser == null) {
            return null;
        }
        return proxiedUser.getCurrentLoginRemoteAddr();
    }

    @Override
    public Date getPreviousLoginDate() {
        ensureUser();
        if (proxiedUser == null) {
            return null;
        }
        return proxiedUser.getPreviousLoginDate();
    }

    @Override
    public String getPreviousLoginRemoteAddr() {
        ensureUser();
        if (proxiedUser == null) {
            return null;
        }
        return proxiedUser.getPreviousLoginRemoteAddr();
    }

    @Override
    public int getLoginCount() {
        ensureUser();
        if (proxiedUser == null) {
            return 0;
        }
        return proxiedUser.getLoginCount();
    }

    @Override
    public UserType getUserType() {
        ensureUser();
        if (proxiedUser == null) {
            return null;
        }
        return proxiedUser.getUserType();
    }

    @Override
    public String getUserStatus() {
        ensureUser();
        if (proxiedUser == null) {
            return null;
        }
        return proxiedUser.getUserStatus();
    }

    @Override
    public String getCurrentWorkspaceId() {
        ensureUser();
        if (proxiedUser == null) {
            return null;
        }
        return proxiedUser.getCurrentWorkspaceId();
    }

    @Override
    public JSONObject getPreferences() {
        ensureUser();
        if (proxiedUser == null) {
            return null;
        }
        return proxiedUser.getPreferences();
    }

    private void ensureUser() {
        if (proxiedUser == null) {
            proxiedUser = userRepository.findById(userId);
        }
    }
}
