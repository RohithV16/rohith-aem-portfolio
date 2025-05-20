package com.rohithaem.portfolio.core.schedulers;

import com.rohithaem.portfolio.core.commons.ResourceResolverService;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.discovery.TopologyEvent;
import org.apache.sling.discovery.TopologyEventListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        service = Runnable.class,
        immediate = true,
        property = {
                "scheduler.expression=*/10 * * * * ?",
                "scheduler.runOn=LEADER"
        }
)
public class Dehibernate implements Runnable, TopologyEventListener {
    private final Logger log = LoggerFactory.getLogger(Dehibernate.class);

    @Reference
    private ResourceResolverService resourceResolverService;
    private boolean isLeader = false;

    @Override
    public void run() {
        // Scheduled services that do not have to be cluster aware do not need
        // to implement this check OR extend TopologyEventListener
        if (!isLeader) {
            return;
        }

        // Scheduled service logic, only run on the Master
        try (ResourceResolver resourceResolver = resourceResolverService.getResourceResolver()) {
            log.error("Resolver {}", resourceResolver.isLive());
            log.error("It is running on author {}", System.currentTimeMillis());
        }
    }

    @Override
    public void handleTopologyEvent(final TopologyEvent event) {
        if (event.getType() == TopologyEvent.Type.TOPOLOGY_CHANGED
                || event.getType() == TopologyEvent.Type.TOPOLOGY_INIT) {
            this.isLeader = event.getNewView().getLocalInstance().isLeader();
        }
    }
}