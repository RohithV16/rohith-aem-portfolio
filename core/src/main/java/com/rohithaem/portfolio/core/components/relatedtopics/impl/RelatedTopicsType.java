//package com.adobe.aem.aklc.react.core.components.global.relatedtopics.impl;
//
//import com.adobe.acs.commons.util.ModeUtil;
//import com.adobe.aem.aklc.react.core.commons.utils.HtmlUtil;
//import com.day.cq.wcm.api.Page;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.sling.api.resource.Resource;
//import org.apache.sling.api.resource.ResourceResolver;
//import org.apache.sling.models.annotations.DefaultInjectionStrategy;
//import org.apache.sling.models.annotations.Model;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//
//import static com.adobe.aem.aklc.react.core.commons.constants.Constants.ACTIVATE;
//import static com.adobe.aem.aklc.react.core.commons.constants.Constants.CARD_URL;
//import static com.adobe.aem.aklc.react.core.commons.constants.Constants.CHILDREN;
//import static com.adobe.aem.aklc.react.core.commons.constants.Constants.CONTENT_TYPE;
//import static com.adobe.aem.aklc.react.core.commons.constants.Constants.DESCRIPTION;
//import static com.adobe.aem.aklc.react.core.commons.constants.Constants.HEADING_CONTENT_TYPE;
//import static com.adobe.aem.aklc.react.core.commons.constants.Constants.HTML_EXTENSION;
//import static com.adobe.aem.aklc.react.core.commons.constants.Constants.LINK;
//import static com.adobe.aem.aklc.react.core.commons.constants.Constants.TAGS;
//import static com.adobe.aem.aklc.react.core.commons.constants.Constants.TITLE;
//import static com.adobe.aem.aklc.react.core.commons.constants.Constants.URL;
//import static com.day.cq.replication.ReplicationStatus.NODE_PROPERTY_LAST_REPLICATION_ACTION;
//
//@Model(adaptables = {Resource.class}, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
//public class RelatedTopicsType {
//
//    private static final String PAGE_ORDER = "pageOrder";
//    private static final String ORDER_TYPE = "orderType";
//    private static final String MANUAL = "manual";
//    private static final String RELATED_TOPIC_ORDER = "relatedtopicorder";
//
//    private static Map<String, String> mappingLinks(String title, String path) {
//        Map<String, String> links = new HashMap<>();
//        links.put(URL, (HtmlUtil.formatUrlWithDotHtml(path)));
//        links.put(CHILDREN, (title));
//        return links;
//    }
//
//    public static List<Map<String, Object>> cards(List<String> manualPages, ResourceResolver resolver) {
//        List<Map<String, Object>> pagePaths = new ArrayList<>();
//
//        for (String autoPage : manualPages) {
//            if (StringUtils.isNotEmpty(autoPage)) {
//                int indexOfDotHtml = autoPage.lastIndexOf(HTML_EXTENSION);
//                String pageName = indexOfDotHtml != -1 ? autoPage.substring(0, indexOfDotHtml) : autoPage;
//                Resource childPage = resolver.getResource(pageName);
//                if (null != childPage) {
//                    Page currentPageProperties = childPage.adaptTo(Page.class);
//                    Map<String, Object> pageProperties = new HashMap<>();
//
//                    pageProperties.put(CARD_URL, autoPage.contains(HTML_EXTENSION) ? autoPage :
//                            HtmlUtil.formatUrlWithDotHtml(currentPageProperties.getPath()));
//                    pageProperties.put(TITLE, currentPageProperties.getTitle());
//                    pageProperties.put(DESCRIPTION, currentPageProperties.getDescription());
//                    pageProperties.put(PAGE_ORDER,
//                            currentPageProperties.getProperties().get(RELATED_TOPIC_ORDER, String.class));
//
//                    String contentType = currentPageProperties.getProperties().get(HEADING_CONTENT_TYPE, String.class);
//                    if (contentType != null && contentType.contentEquals("Guide")) {
//                        pageProperties.put(CONTENT_TYPE,
//                                currentPageProperties.getProperties().get(HEADING_CONTENT_TYPE, String.class));
//                        pageProperties.put("footerTextDescription", "This guide has X steps");
//                    }
//
//                    pageProperties.put(ORDER_TYPE, MANUAL);
//                    populatePagePathBasedOnEnvironments(pagePaths, currentPageProperties, pageProperties);
//                }
//            }
//        }
//        return pagePaths;
//    }
//
//    private static void populatePagePathBasedOnEnvironments(List<Map<String, Object>> pagesPaths,
//                                                            Page currentPageProperties,
//                                                            Map<String, Object> pageProperties) {
//        if (ModeUtil.isAuthor()) {
//            if (null != currentPageProperties.getProperties().get(NODE_PROPERTY_LAST_REPLICATION_ACTION, String.class)
//                    && Objects.requireNonNull(
//                            currentPageProperties.getProperties().get(NODE_PROPERTY_LAST_REPLICATION_ACTION, String.class))
//                    .contentEquals(ACTIVATE)) {
//                pagesPaths.add(pageProperties);
//            }
//        } else {
//            pagesPaths.add(pageProperties);
//        }
//    }
//
//    public static List<Map<String, Object>> links(List<String> manualPages, ResourceResolver resolver) {
//        List<Map<String, Object>> pagesPaths = new ArrayList<>();
//        for (String autoPage : manualPages) {
//            if (StringUtils.isNotEmpty(autoPage)) {
//                int indexOfDotHtml = autoPage.lastIndexOf(HTML_EXTENSION);
//                String pageName = indexOfDotHtml != -1 ? autoPage.substring(0, indexOfDotHtml) : autoPage;
//                Resource childPage = resolver.getResource(pageName);
//                if (null != childPage) {
//                    Page currentPageProperties = childPage.adaptTo(Page.class);
//                    Map<String, Object> pageProperties = new HashMap<>();
//                    pageProperties.put(LINK,
//                            mappingLinks(currentPageProperties.getTitle(), currentPageProperties.getPath()));
//                    pageProperties.put(CARD_URL, autoPage.contains(HTML_EXTENSION) ? autoPage :
//                            HtmlUtil.formatUrlWithDotHtml(currentPageProperties.getPath()));
//                    pageProperties.put(PAGE_ORDER,
//                            currentPageProperties.getProperties().get(RELATED_TOPIC_ORDER, String.class));
//                    pageProperties.put(TITLE, currentPageProperties.getTitle());
//                    pageProperties.put(ORDER_TYPE, MANUAL);
//                    populatePagePathBasedOnEnvironments(pagesPaths, currentPageProperties, pageProperties);
//                }
//            }
//        }
//        return pagesPaths;
//    }
//
//    public static List<Map<String, Object>> linksWithDescription(List<String> manualPages, ResourceResolver resolver) {
//        List<Map<String, Object>> pagesPaths = new ArrayList<>();
//        for (String autoPage : manualPages) {
//            if (StringUtils.isNotEmpty(autoPage)) {
//                int indexOfDotHtml = autoPage.lastIndexOf(HTML_EXTENSION);
//                String pageName = indexOfDotHtml != -1 ? autoPage.substring(0, indexOfDotHtml) : autoPage;
//                Resource childPage = resolver.getResource(pageName);
//                if (null != childPage) {
//                    Page currentPageProperties = childPage.adaptTo(Page.class);
//                    Map<String, Object> pageProperties = new HashMap<>();
//                    pageProperties.put(TITLE, currentPageProperties.getTitle());
//                    pageProperties.put(LINK,
//                            mappingLinks(currentPageProperties.getTitle(), currentPageProperties.getPath()));
//                    pageProperties.put(DESCRIPTION, currentPageProperties.getDescription());
//                    pageProperties.put(CARD_URL, autoPage.contains(HTML_EXTENSION) ? autoPage :
//                            HtmlUtil.formatUrlWithDotHtml(currentPageProperties.getPath()));
//                    pageProperties.put(PAGE_ORDER,
//                            currentPageProperties.getProperties().get(RELATED_TOPIC_ORDER, String.class));
//                    pageProperties.put(ORDER_TYPE, MANUAL);
//                    populatePagePathBasedOnEnvironments(pagesPaths, currentPageProperties, pageProperties);
//                }
//            }
//        }
//        return pagesPaths;
//    }
//
//    public static Map<String, Object> tagsCards(Page page) {
//        Map<String, Object> pageProperties = new HashMap<>();
//        pageProperties.put(CARD_URL, HtmlUtil.formatUrlWithDotHtml(page.getPath()));
//        pageProperties.put(TITLE, page.getTitle());
//        pageProperties.put(DESCRIPTION, page.getDescription());
//        String contentType = page.getProperties().get(HEADING_CONTENT_TYPE, String.class);
//        if (contentType != null && contentType.contentEquals("Guide")) {
//            pageProperties.put(CONTENT_TYPE, page.getProperties().get(HEADING_CONTENT_TYPE, String.class));
//            pageProperties.put("footerTextDescription", "This guide has X steps");
//        }
//        pageProperties.put(PAGE_ORDER, page.getProperties().get(RELATED_TOPIC_ORDER, String.class));
//        pageProperties.put(ORDER_TYPE, TAGS);
//        return pageProperties;
//    }
//
//    public static Map<String, Object> tagsLink(Page page) {
//        Map<String, Object> pageProperties = new HashMap<>();
//        pageProperties.put(LINK, mappingLinks(page.getTitle(), page.getPath()));
//        pageProperties.put(CARD_URL, HtmlUtil.formatUrlWithDotHtml(page.getPath()));
//        pageProperties.put(PAGE_ORDER, page.getProperties().get(RELATED_TOPIC_ORDER, String.class));
//        pageProperties.put(TITLE, page.getTitle());
//        pageProperties.put(ORDER_TYPE, TAGS);
//        return pageProperties;
//    }
//
//    public static Map<String, Object> tagsLinksWithDescription(Page page) {
//        Map<String, Object> pageProperties = new HashMap<>();
//        pageProperties.put(LINK, mappingLinks(page.getTitle(), page.getPath()));
//        pageProperties.put(DESCRIPTION, page.getDescription());
//        pageProperties.put(CARD_URL, HtmlUtil.formatUrlWithDotHtml(page.getPath()));
//        pageProperties.put(TITLE, page.getTitle());
//        pageProperties.put(PAGE_ORDER, page.getProperties().get(RELATED_TOPIC_ORDER, String.class));
//        pageProperties.put(ORDER_TYPE, TAGS);
//        return pageProperties;
//    }
//
//}
