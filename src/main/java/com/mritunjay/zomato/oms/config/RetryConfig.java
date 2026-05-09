package com.mritunjay.zomato.oms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class RetryConfig {

    // Single place to enable Spring Retry across the application.
    // @Retryable and @Recover annotations will not work without this.

}
