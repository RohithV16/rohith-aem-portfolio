package com.rohithaem.portfolio.core.components.relatedtopics.impl;


import com.adobe.acs.commons.util.ModeUtil;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.rohithaem.portfolio.core.components.relatedtopics.RelatedTopicsModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.search.eval.JcrPropertyPredicateEvaluator.PROPERTY;
import static com.day.cq.tagging.TagConstants.PN_TAGS;
import static com.day.cq.wcm.api.NameConstants.NT_PAGE;
import static java.lang.Math.min;


@Model(adaptables = SlingHttpServletRequest.class, adapters = {RelatedTopicsModel.class,
        ComponentExporter.class}, resourceType = RelatedTopicsImpl.RESOURCE_TYPE, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class RelatedTopicsImpl implements RelatedTopicsModel {
    static final String RESOURCE_TYPE = "aklc-spa-react/components/global/relatedtopics";
    private static final String CARDS = "cards";
    private static final String VALUE = "_value";
    @Inject
    Resource resource;
    @ValueMapValue
    private String rollupColumnFlow;
    @ValueMapValue
    private Integer rollupColumnCount;
    @ValueMapValue
    @Default(values = OR)
    private String operator;
    @ValueMapValue
    @Default(intValues = 6)
    private Integer numberOfResults;
    @ValueMapValue
    private Boolean orderByTitle;
    @ValueMapValue
    private String[] topics;
    @ValueMapValue
    private String[] location;
    @ValueMapValue
    private String[] body;
    @ValueMapValue
    private String[] audience;
    @ValueMapValue
    private String rollupType;
    @Inject
    private ResourceResolver resourceResolver;

    @Override
    public String getExportedType() {
        return RelatedTopicsImpl.RESOURCE_TYPE;
    }


    @Override
    public String getRollupColumnFlow() {
        return rollupColumnFlow;
    }

    @Override
    public Integer getRollupColumnCount() {
        return rollupColumnCount;
    }

    @Override
    public Integer getNumberOfResults() {
        return numberOfResults;
    }

    @Override
    public Boolean getOrderByTitle() {
        return orderByTitle;
    }

    @JsonIgnore
    @Override
    public List<String> getQueryResultPaths() {
        List<String> queryResultPages = new ArrayList<>();
        try {
            QueryBuilder queryBuilder = resourceResolver.adaptTo(QueryBuilder.class);
            final Map<String, Object> mapForQueryBuilder = new HashMap<>();
            mapForQueryBuilder.put("type", NT_PAGE);
            mapForQueryBuilder.put("path", "/content");
            if (ModeUtil.isAuthor()) {
                mapForQueryBuilder.put(PROPERTY, "jcr:content/cq:lastReplicationAction");
                mapForQueryBuilder.put("property.value", "Activate");
            }
            mapForQueryBuilder.put("group.p." + operator, "true");
            populateGroup1Properties(mapForQueryBuilder);
            populateGroup2Properties(mapForQueryBuilder);
            populateGroup3Properties(mapForQueryBuilder);
            populateGroup4Properties(mapForQueryBuilder);
            mapForQueryBuilder.put("orderby", "@jcr:content/jcr:title");
            mapForQueryBuilder.put("orderby.sort", "asc");
            mapForQueryBuilder.put("p.limit", "-1");
            if (topics != null || audience != null || location != null || body != null) {
                if (null != queryBuilder) {
                    final Query query = queryBuilder.createQuery(PredicateGroup.create(mapForQueryBuilder),
                            resourceResolver.adaptTo(Session.class));
                    final SearchResult result = query.getResult();
                    for (Hit hit : result.getHits()) {
                        queryResultPages.add(hit.getPath());
                    }
                }
            }
        } catch (RepositoryException e) {
            log.error("RepositoryException at RelatedTopicsImpl {}", e.getMessage());
        }
        return queryResultPages;
    }


    private void populateGroup1Properties(Map<String, Object> mapForQueryBuilder) {
        mapForQueryBuilder.put("group.1_property", "jcr:content/cq:tags");
        mapForQueryBuilder.put("group.1_property.p." + operator, "true");
        if (topics != null && topics.length != 0) {
            for (int i = 0; i < topics.length; i++) {
                mapForQueryBuilder.put("group.1_property." + i + "value", topics[i]);
            }
        }
    }

    private void populateGroup2Properties(Map<String, Object> mapForQueryBuilder) {
        mapForQueryBuilder.put("group.2_property", "jcr:content/cq:tags");
        mapForQueryBuilder.put("group.2_property.p." + operator, "true");
        if (audience != null && audience.length != 0) {
            for (int i = 0; i < audience.length; i++) {
                mapForQueryBuilder.put("group.2_property." + i + "value", audience[i]);
            }
        }
    }

    private void populateGroup4Properties(Map<String, Object> mapForQueryBuilder) {
        mapForQueryBuilder.put("group.4_property", "jcr:content/cq:tags");
        mapForQueryBuilder.put("group.4_property.p." + operator, "true");
        if (body != null && body.length != 0) {
            for (int i = 0; i < body.length; i++) {
                mapForQueryBuilder.put("group.4_property." + i + VALUE, body[i]);
            }
        }
    }

    private void populateGroup3Properties(Map<String, Object> mapForQueryBuilder) {
        mapForQueryBuilder.put("group.3_property", "jcr:content/cq:tags");
        mapForQueryBuilder.put("group.3_property.p." + operator, "true");
        if (location != null && location.length != 0) {
            for (int i = 0; i < location.length; i++) {
                mapForQueryBuilder.put("group.3_property." + i + VALUE, location[i]);
            }
        }
    }

    private List<Map<String, Object>> getManual() {
        List<Map<String, Object>> manual = new ArrayList<>();
        List<String> manualPages = new ArrayList<>();
        Resource contentResource = resource.getChild("manualItems");
        if (contentResource != null) {
            for (Resource items : contentResource.getChildren()) {
                manualPages.add(items.getValueMap().get("manualpageurl", String.class));
            }
        }
        return manual;
    }

    private List<Map<String, Object>> getTags() {
        List<Map<String, Object>> pageListTags = new ArrayList<>();
        List<String> cpathList = getQueryResultPaths();
        if (cpathList != null && !cpathList.isEmpty()) {
            for (String cardUrl : cpathList) {
                Resource rootResource1 = resourceResolver.getResource(cardUrl);
                if (rootResource1 != null) {
                    PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
                    populatePageListTags(cardUrl, pageManager, pageListTags);
                }
            }
        }
        return pageListTags;
    }

    private void populatePageListTags(String cardUrl, PageManager pageManager, List<Map<String, Object>> pageListTags) {
        if (pageManager != null && pageManager.getPage(cardUrl) != null && null != rollupType) {
            Page page = pageManager.getPage(cardUrl);
            if (rollupType.equalsIgnoreCase(CARDS)) {
                pageListTags.add(RelatedTopicsType.tagsCards(page));
            } else if (rollupType.equalsIgnoreCase(LINK)) {
                pageListTags.add(RelatedTopicsType.tagsLink(page));
            } else {
                pageListTags.add(RelatedTopicsType.tagsLinksWithDescription(page));
            }
        }
    }

    private List<Map<String, Object>> getResult() {
        List<Map<String, Object>> manualFinalResult = new ArrayList<>();
        List<Map<String, Object>> manualResult = getManual();
        List<Map<String, Object>> tagsResult = getTags();
        Set<String> hashSet = new HashSet<>();
        if (manualResult.size() == numberOfResults) {
            return manualResult;
        } else if (manualResult.size() < numberOfResults) {
            int remain = min((numberOfResults - manualResult.size()), tagsResult.size());
            for (Map<String, Object> cardUrl : manualResult) {
                hashSet.add((String) cardUrl.get("cardUrl"));
            }
            for (int j = 0; j < remain; j++) {
                if (!hashSet.contains(tagsResult.get(j).get(CARD_URL))) {
                    manualResult.add(tagsResult.get(j));
                }
            }
            return manualResult;
        } else {
            for (int i = 0; i < numberOfResults; i++) {
                manualFinalResult.add(manualResult.get(i));
            }
            return manualFinalResult;
        }
    }

    @Override
    public List<Map<String, Object>> getRollupLinks() {
        return (null != rollupType && rollupType.equalsIgnoreCase(LINK)) ||
                (null != rollupType && rollupType.equalsIgnoreCase("desc"))
                ? getResult() : Collections.emptyList();
    }
}
