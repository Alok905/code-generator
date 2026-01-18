package com.alok.projects.lovable_clone.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {

    @Value("${stripe.api.secret}")
    private String stripeSecretKey;

    /**
     * @PostConstruct : it is run after bean is created and dependencies are injected; but before application starts serving requests.
     * here, this init() method is used to connect with the stripe account after the application starts and before serving the requests.
     */
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }
}
