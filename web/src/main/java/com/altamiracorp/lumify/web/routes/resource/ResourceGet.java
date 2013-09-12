package com.altamiracorp.lumify.web.routes.resource;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.resources.Resource;
import com.altamiracorp.lumify.model.resources.ResourceRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResourceGet extends BaseRequestHandler {
    private ResourceRepository resourceRepository = new ResourceRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String rowKey = getAttributeString(request, "_rowKey");
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
}
