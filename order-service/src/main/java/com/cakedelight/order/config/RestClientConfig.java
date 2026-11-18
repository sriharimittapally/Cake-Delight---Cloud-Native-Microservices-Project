
package com.cakedelight.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configures the RestClient used for communication with other services.
 */
@Configuration
public class RestClientConfig {

    /**
     * Provides a RestClient.Builder bean for creating REST clients.
     */
    @Bean
    public RestClient.Builder restClientBuilder() {

        return RestClient.builder();
    }
}
