package com.rohithaem.portfolio.core.listeners;

import com.day.cq.replication.ReplicationAction;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        service = EventHandler.class,
        immediate = true,
        property = {
                EventConstants.EVENT_TOPIC + "=" + ReplicationAction.EVENT_TOPIC,
        })
public class PublishEventHandler implements EventHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void handleEvent(Event event) {
        logger.error("PropertiesRohith {} {}", event.getPropertyNames(), event.getTopic());
        ReplicationAction re = ReplicationAction.fromEvent(event);
        logger.error("ReplicationPropertiesRohith {}", event.getPropertyNames());
    }
}
