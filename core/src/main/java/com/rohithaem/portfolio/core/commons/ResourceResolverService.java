package com.rohithaem.portfolio.core.commons;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Component(service = ResourceResolverService.class, immediate = true)
public class ResourceResolverService {

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SERVICE_USER = "resource-Resolver-user";

    public ResourceResolver getResourceResolver() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(ResourceResolverFactory.SUBSERVICE, SERVICE_USER);

        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = resourceResolverFactory.getServiceResourceResolver(paramMap);
        } catch (LoginException e) {
            logger.error(e.getMessage());
        }
        return resourceResolver;
    }
}
