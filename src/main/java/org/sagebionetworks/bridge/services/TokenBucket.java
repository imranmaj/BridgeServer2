package org.sagebionetworks.bridge.services;

import org.joda.time.Instant;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class TokenBucket {
    private static final long DEFAULT_MAXIMUM_TOKENS = 1 * 1000 * 1000 * 1000; // 1 GB
    private static final int DEFAULT_REFILL_INTERVAL_MILLISECONDS = 3600 * 1000; // every hr
    private static final long DEFAULT_REFILL_AMOUNT = 1 * 1000 * 1000; // 1 MB

    private long maximumTokens;
    private int refillIntervalMilliseconds;
    private long refillAmount;
    private Map<String, Long> tokens = new ConcurrentHashMap<>();
    private Instant lastRefill = Instant.now();

    public TokenBucket() {
        this.maximumTokens = DEFAULT_MAXIMUM_TOKENS;
        this.refillIntervalMilliseconds = DEFAULT_REFILL_INTERVAL_MILLISECONDS;
        this.refillAmount = DEFAULT_REFILL_AMOUNT;
    }

    public TokenBucket(int maximumTokens, int refillIntervalMilliseconds, int refillAmount) {
        this.maximumTokens = maximumTokens;
        this.refillIntervalMilliseconds = refillIntervalMilliseconds;
        this.refillAmount = refillAmount;
    }

    private void refreshTokens() {
        int sinceLastRefillMilliseconds = Instant.now().compareTo(lastRefill);
        long tokensToRefill = (long)(sinceLastRefillMilliseconds / refillIntervalMilliseconds) * refillAmount;
        if (tokensToRefill > 0) {
            tokens.forEach(
                    (resourceKey, tokenCount) -> tokenCount = Math.min(maximumTokens, tokenCount + tokensToRefill));
        }
    }

    public boolean tryGetResource(String resourceKey, long tokenDecrement) {
        refreshTokens();

        tokens.putIfAbsent(resourceKey, maximumTokens);
        long resourceCurrentTokens = tokens.get(resourceKey);
        if (tokens.get(resourceKey) >= tokenDecrement) {
            tokens.put(resourceKey, resourceCurrentTokens - tokenDecrement);
            return true;
        }
        return false;
    }
}
