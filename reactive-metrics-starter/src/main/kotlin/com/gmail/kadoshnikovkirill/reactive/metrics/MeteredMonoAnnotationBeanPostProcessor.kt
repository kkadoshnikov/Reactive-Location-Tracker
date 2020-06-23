package com.gmail.kadoshnikovkirill.reactive.metrics

import io.micrometer.core.instrument.LongTaskTimer
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.slf4j.LoggerFactory
import org.springframework.aop.Pointcut
import org.springframework.aop.framework.autoproxy.AbstractBeanFactoryAwareAdvisingPostProcessor
import org.springframework.aop.support.DefaultPointcutAdvisor
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut
import org.springframework.core.annotation.AnnotationUtils
import reactor.core.publisher.Mono

class MeteredMonoAnnotationBeanPostProcessor(private val registry: MeterRegistry) : AbstractBeanFactoryAwareAdvisingPostProcessor() {

    val log = LoggerFactory.getLogger(javaClass)

    init {
        val pointcut: Pointcut = AnnotationMatchingPointcut(null, MeteredMono::class.java, true)
        advisor = DefaultPointcutAdvisor(pointcut, meteredMonoAdvice())
        isProxyTargetClass = true
    }

    private fun meteredMonoAdvice(): MethodInterceptor {
        return MethodInterceptor(fun(invocation: MethodInvocation): Any? {
            val method = invocation.method
            val metered = AnnotationUtils.findAnnotation(method, MeteredMono::class.java)
                    ?: return invocation.proceed()
            val retVal = invocation.proceed() as Mono<*>
            return if (metered.longTask) {
                processWithLongTask(retVal, metered)
            } else {
                processWithTask(retVal, metered)
            }
        })
    }

    private fun processWithTask(retVal: Mono<*>, metered: MeteredMono): Any {
        val sample = Timer.start(registry)
        return retVal.doOnSuccessOrError { _, t ->
            sample.stop(Timer.builder(metered.value)
                    .description(if (metered.description.isEmpty()) null else metered.description)
                    .tags(*metered.extraTags)
                    .tag("exception", t?.javaClass?.simpleName ?: "none")
                    .publishPercentileHistogram(metered.histogram)
                    .publishPercentiles(*metered.percentiles)
                    .register(registry))
        }
    }

    private fun processWithLongTask(retVal: Mono<*>, metered: MeteredMono): Any {
        val sample = buildLongTaskTimer(metered)?.start()
        return retVal.doOnTerminate { sample?.stop() }
    }

    /**
     * Secure long task timer creation - it should not disrupt the application flow in case of exception
     */
    private fun buildLongTaskTimer(metered: MeteredMono): LongTaskTimer? {
        return try {
            LongTaskTimer.builder(metered.value)
                    .description(if (metered.description.isEmpty()) null else metered.description)
                    .tags(*metered.extraTags)
                    .register(registry)
        } catch (e: Exception) {
            log.warn("Unable to create long task timer for $metered", e)
            null
        }
    }
}