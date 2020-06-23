package com.gmail.kadoshnikovkirill.reactive.metrics;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.autoproxy.AbstractBeanFactoryAwareAdvisingPostProcessor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.aopalliance.intercept.MethodInterceptor;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

public class MeteredMonoAnnotationBeanPostProcessor extends AbstractBeanFactoryAwareAdvisingPostProcessor {

    private final MeterRegistry registry;

    public MeteredMonoAnnotationBeanPostProcessor(MeterRegistry registry) {
        this.registry = registry;
        Pointcut pointcut = new AnnotationMatchingPointcut(null, MeteredMono.class, true);
        this.advisor = new DefaultPointcutAdvisor(pointcut, meteredMonoAdvice());
        setProxyTargetClass(true);
    }

    private MethodInterceptor meteredMonoAdvice() {
        return (invocation) -> {
            Method method = invocation.getMethod();
            MeteredMono metered = findAnnotation(method, MeteredMono.class);
            if (metered == null) {
                return invocation.proceed();
            }
            Mono<?> retVal = (Mono) invocation.proceed();
            if (!metered.longTask()) {
                return processWithTask(retVal, metered);
            } else {
                return processWithLongTask(retVal, metered);
            }
        };
    }

    private Object processWithTask(Mono<?> retVal, MeteredMono metered) {
        Timer.Sample sample = Timer.start(registry);

        return retVal.doOnSuccessOrError((obj, t) -> sample.stop(Timer.builder(metered.value())
                .description(metered.description().isEmpty() ? null : metered.description())
                .tags(metered.extraTags())
                .tag("isEmpty", t == null ? "false" : "true")
                .tag("exception", t == null ? "none" : t.getClass().getSimpleName())
                .publishPercentileHistogram(metered.histogram())
                .publishPercentiles(metered.percentiles().length == 0 ? null : metered.percentiles())
                .register(registry)));
    }

    private Object processWithLongTask(Mono<?> retVal, MeteredMono metered) {
        Optional<LongTaskTimer.Sample> sample = buildLongTaskTimer(metered).map(LongTaskTimer::start);
        return retVal.doOnSuccessOrError((obj, t) -> sample.ifPresent(LongTaskTimer.Sample::stop));
    }

    /**
     * Secure long task timer creation - it should not disrupt the application flow in case of exception
     */
    private Optional<LongTaskTimer> buildLongTaskTimer(MeteredMono metered) {
        try {
            return Optional.of(LongTaskTimer.builder(metered.value())
                    .description(metered.description().isEmpty() ? null : metered.description())
                    .tags(metered.extraTags())
                    .register(registry));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
