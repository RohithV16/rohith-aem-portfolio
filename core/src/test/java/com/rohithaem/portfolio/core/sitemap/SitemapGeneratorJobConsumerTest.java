package com.rohithaem.portfolio.core.sitemap;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer.JobResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class SitemapGeneratorJobConsumerTest {

    private final AemContext context = new AemContext();

    @Mock
    private Job job;

    private SitemapGeneratorJobConsumer sitemapGeneratorJobConsumer;

    @BeforeEach
    void setUp() {
        sitemapGeneratorJobConsumer = new SitemapGeneratorJobConsumer();
    }

    @Test
    void testProcessJob_Success() {
        when(job.getProperty("test", String.class)).thenReturn("trigerred from the scheduler");
        JobResult result = sitemapGeneratorJobConsumer.process(job);
        assertEquals(JobResult.OK, result);
    }

    @Test
    void testProcessJob_Failure() {
        // Arrange
        when(job.getProperty("test", String.class)).thenReturn("some other value");

        // Act
        JobResult result = sitemapGeneratorJobConsumer.process(job);

        // Assert
        assertEquals(JobResult.FAILED, result);
    }

    @Test
    void testProcessJob_Exception() {
        // Arrange
        when(job.getProperty("test", String.class)).thenThrow(new RuntimeException("Test exception"));

        // Act
        JobResult result = sitemapGeneratorJobConsumer.process(job);

        // Assert
        assertEquals(JobResult.FAILED, result);
    }
} 