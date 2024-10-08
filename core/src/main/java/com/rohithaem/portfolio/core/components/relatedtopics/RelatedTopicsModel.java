package com.rohithaem.portfolio.core.components.relatedtopics;

import com.adobe.cq.export.json.ComponentExporter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface RelatedTopicsModel extends ComponentExporter {
    String getRollupColumnFlow();

    Integer getRollupColumnCount();

    Integer getNumberOfResults();

    Boolean getOrderByTitle();

    List<Map<String, Object>> getRollupLinks();
}
