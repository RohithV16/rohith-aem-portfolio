package com.rohithaem.portfolio.core.schedulers;

import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = AiTest.class)
@Designate(ocd = AiTest.Config.class)
public class AiTest implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiTest.class);
    private int schedulerId;

    @Reference
    private Scheduler scheduler;

    @ObjectClassDefinition(name = "AI Test Scheduler Configuration", 
                         description = "Scheduler configuration for AI Test")
    public @interface Config {
        @AttributeDefinition(name = "Scheduler name", 
                           description = "Name of the scheduler")
        String schedulerName() default "ai-test-scheduler";

        @AttributeDefinition(name = "Enabled", 
                           description = "Enable/disable the scheduler")
        boolean enabled() default true;

        @AttributeDefinition(name = "Run on Leader Only", 
                           description = "Run the scheduler only on leader instance")
        boolean runOnLeader() default true;
    }

    @Activate
    protected void activate(Config config) {
        schedulerId = config.schedulerName().hashCode();
        addScheduler(config);
    }

    @Deactivate
    protected void deactivate(Config config) {
        removeScheduler();
    }

    private void addScheduler(Config config) {
        if (config.enabled()) {
            ScheduleOptions scheduleOptions = scheduler.EXPR("0/10 * * * * ?");
            scheduleOptions.name(String.valueOf(schedulerId));
            scheduleOptions.canRunConcurrently(false);
            
            // Set to run only on leader
            if (config.runOnLeader()) {
                scheduleOptions.onLeaderOnly(true);
            }
            
            scheduler.schedule(this, scheduleOptions);
            LOGGER.info("Scheduler added with name: {}", config.schedulerName());
        } else {
            LOGGER.info("Scheduler {} is disabled", config.schedulerName());
        }
    }

    private void removeScheduler() {
        scheduler.unschedule(String.valueOf(schedulerId));
        LOGGER.info("Scheduler removed");
    }

    @Override
    public void run() {
        try {
            LOGGER.error("AI Test Scheduler running at: {}", System.currentTimeMillis());
        } catch (Exception e) {
            LOGGER.error("Error in AI Test Scheduler: {}", e.getMessage(), e);
        }
    }
} 