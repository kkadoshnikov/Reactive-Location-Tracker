package com.gmail.kadoshnikovkirill.reactive.metrics;

import io.micrometer.core.instrument.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactiveMetricsConfiguration {

    @Bean
    @Autowired
    public MeteredMonoAnnotationBeanPostProcessor meteredMonoAnnotationBeanPostProcessor() {
        return new MeteredMonoAnnotationBeanPostProcessor(Metrics.globalRegistry);
    }
}
