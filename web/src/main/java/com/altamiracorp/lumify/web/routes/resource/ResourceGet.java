package com.altamiracorp.lumify.web.routes.resource;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.resources.Resource;
import com.altamiracorp.lumify.model.resources.ResourceRepository;
import com.altamiracorp.lumify.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import com.altamiracorp.web.utils.UrlUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResourceGet implements Handler, AppAware {
    private ResourceRepository resourceRepository = new ResourceRepository();
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String rowKey = getRequiredAttribute(request, "_rowKey");
        AppSession session = app.getAppSession(request);

        Resource resource = resourceRepository.findByRowKey(session.getModelSession(), rowKey);
        if (resource == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(resource.getContent().getContentType());
        ServletOutputStream out = response.getOutputStream();
        out.write(resource.getContent().getData());
        out.close();
    }

    public static String getRequiredAttribute(HttpServletRequest request, String parameterName) {
        String attribute = (String) request.getAttribute(parameterName);
        if (attribute == null) {
            throw new RuntimeException("'" + parameterName + "' is required.");
        }
        return UrlUtils.urlDecode(attribute);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
