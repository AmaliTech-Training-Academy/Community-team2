package com.amalitech.communityboard.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("within(com.amalitech.communityboard.service..*)")
    public void serviceLayer() {}

    @Before("serviceLayer()")
    public void logBefore(org.aspectj.lang.JoinPoint jp) {
        Signature sig = jp.getSignature();
        log.info("[BEFORE] {}.{}() called with args={}",
                sig.getDeclaringTypeName(), sig.getName(), Arrays.toString(jp.getArgs()));
    }

    @After("serviceLayer()")
    public void logAfter(org.aspectj.lang.JoinPoint jp) {
        Signature sig = jp.getSignature();
        log.info("[AFTER] {}.{}() finished",
                sig.getDeclaringTypeName(), sig.getName());
    }

    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    public void logException(org.aspectj.lang.JoinPoint jp, Throwable ex) {
        Signature sig = jp.getSignature();
        log.error("[EXCEPTION] {}.{}() threw {}: {}",
                sig.getDeclaringTypeName(), sig.getName(), ex.getClass().getSimpleName(), ex.getMessage());
    }

    @Around("serviceLayer()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        Signature sig = pjp.getSignature();
        long start = System.nanoTime();
        try {
            Object result = pjp.proceed();
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            log.info("[AROUND] {}.{}() returned ({} ms)", sig.getDeclaringTypeName(), sig.getName(), durationMs);

            return result;
        } catch (Throwable t) {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            log.error("[AROUND] {}.{}() threw {} after {} ms",
                    sig.getDeclaringTypeName(), sig.getName(), t.getClass().getSimpleName(), durationMs);
            throw t;
        }
    }
}
