package com.rohithaem.portfolio.core.models;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Defines the {@code ChatbotModel} Sling Model used for the AEM RAG chatbot component.
 */
@ProviderType
public interface ChatbotModel {

    String getTitle();

    String getApiEndpoint();

    String getNamespace();

    String getWelcomeMessage();

    boolean isEmpty();
}
