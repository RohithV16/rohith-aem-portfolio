
package com.rohithaem.portfolio.core.servlets;


import com.rohithaem.portfolio.core.commons.ResourceResolverService;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import javax.servlet.Servlet;
import java.io.IOException;

@Component(service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Simple Demo Servlet to check the service resource Resolver",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET,
                "sling.servlet.paths=/bin/resolverservicetest",
                "sling.servlet.extensions=" + "txt"
        })
public class ResolverServiceTest extends SlingSafeMethodsServlet {

    @Reference
    private ResourceResolverService resourceResolverService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        try (ResourceResolver resourceResolver = resourceResolverService.getResourceResolver()) {
            response.getWriter().write("check\n");
            response.getWriter().write(String.valueOf(System.currentTimeMillis()));
            response.getWriter().write("\nResource Resolver obtained successfully!");
        } catch (PersistenceException e) {
            response.getWriter().write("\nFailed to get Resource Resolver");
        }
    }
}

