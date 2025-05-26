package com.rohithaem.portfolio.core.sitemap;

import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = JobConsumer.class, property = {
    JobConsumer.PROPERTY_TOPICS + "=com/example/sitemap/generate"
})
public class SitemapGeneratorJobConsumer implements JobConsumer {
    
    private static final Logger LOG = LoggerFactory.getLogger(SitemapGeneratorJobConsumer.class);
    
    // @Reference
    // private SitemapGeneratorService sitemapService;
    
    @Override
    public JobResult process(Job job) {
        try {
            String test = job.getProperty("test", String.class);
            if(test.equals("trigerred from the scheduler")){
                LOG.error("Sitemap generation job started",System.currentTimeMillis());
                return JobResult.OK;
            }else{
                LOG.error("Sitemap generation job failed",System.currentTimeMillis());
                return JobResult.FAILED;
            }
            
        } catch (Exception e) {
            LOG.error("Sitemap generation job failed", e);
            return JobResult.FAILED;
        }
    }
}
