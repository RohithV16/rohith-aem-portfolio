package com.rohithaem.portfolio.core.models.impl;

import com.rohithaem.portfolio.core.models.ChatbotModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.commons.lang3.StringUtils;

@Model(
        adaptables = {SlingHttpServletRequest.class, Resource.class},
        adapters = {ChatbotModel.class},
        resourceType = ChatbotModelImpl.RESOURCE_TYPE,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class ChatbotModelImpl implements ChatbotModel {

    protected static final String RESOURCE_TYPE = "rohithaem-portfolio/components/chatbot";

    @ValueMapValue
    private String title;

    @ValueMapValue
    @Default(values = "http://localhost:8080/api/rag/ask/stream")
    private String apiEndpoint;

    @ValueMapValue
    @Default(values = "default")
    private String namespace;

    @ValueMapValue
    @Default(values = "Hello! I am the AEM RAG Chatbot. How can I help you today?")
    private String welcomeMessage;

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getApiEndpoint() {
        return apiEndpoint;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isBlank(title);
    }
}
