package com.gmail.kadoshnikovkirill.reactive.metrics

import io.micrometer.core.instrument.Metrics
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ReactiveMetricsConfiguration {
    @Bean
    fun meteredMonoAnnotationBeanPostProcessor(): MeteredMonoAnnotationBeanPostProcessor {
        return MeteredMonoAnnotationBeanPostProcessor(Metrics.globalRegistry)
    }
}