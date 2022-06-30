package org.sagebionetworks.bridge.services;

import org.joda.time.Instant;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * A rate limiter using the "token bucket" strategy. Resources are mapped to
 * buckets which have a number of tokens in them. Buckets are periodically
 * refilled with tokens.
 * 
 * When a resource is acquired, the tokens in its associated bucket are
 * decremented. If the number of tokens in a bucket is less than the decrement
 * amount, the resource cannot be acquired.
 */
@Component
public class TokenBucketRateLimiter {
    // The default value for maximumTokens (see below).
    private static final long DEFAULT_MAXIMUM_TOKENS = 1 * 1000 * 1000 * 1000; // 1 GB
    // The default value for refillIntervalMilliseconds (see below).
    private static final int DEFAULT_REFILL_INTERVAL_MILLISECONDS = 3600 * 1000; // every hr
    // The default value for refillAmount (see below).
    private static final long DEFAULT_REFILL_AMOUNT = 1 * 1000 * 1000; // 1 MB

    // The maximum number of tokens that can be stored in each bucket. This is also
    // the default number of tokens in each bucket.
    private long maximumTokens;
    // The time between token refills for every bucket in milliseconds.
    private int refillIntervalMilliseconds;
    // The number of tokens that are placed into each bucket during a refill.
    private long refillAmount;
    // Collection which maps resource keys to buckets (represented by a number
    // of tokens in the corresponding bucket).
    private Map<String, Long> buckets = new ConcurrentHashMap<>();
    // The instant at which the last refill occurred.
    private Instant lastRefill = Instant.now();

    /**
     * Class constructor which uses the default values for maximumTokens,
     * refillIntervalMilliseconds, and refillAmount.
     */
    public TokenBucketRateLimiter() {
        this.maximumTokens = DEFAULT_MAXIMUM_TOKENS;
        this.refillIntervalMilliseconds = DEFAULT_REFILL_INTERVAL_MILLISECONDS;
        this.refillAmount = DEFAULT_REFILL_AMOUNT;
    }

    /**
     * Class constructor specifying maximumTokens,
     * refillIntervalMilliseconds, and refillAmount.
     * 
     * @param maximumTokens              The maximum number of tokens that can be
     *                                   stored in each
     *                                   bucket. This is also the default number of
     *                                   tokens in
     *                                   each bucket.
     * @param refillIntervalMilliseconds The time between token refills for every
     *                                   bucket in milliseconds.
     * @param refillAmount               The number of tokens that are placed into
     *                                   each bucket during a refill.
     */
    public TokenBucketRateLimiter(int maximumTokens, int refillIntervalMilliseconds, int refillAmount) {
        this.maximumTokens = maximumTokens;
        this.refillIntervalMilliseconds = refillIntervalMilliseconds;
        this.refillAmount = refillAmount;
    }

    /**
     * Refills tokens into buckets based upon the number of refill intervals that
     * have occurred since the last refill and the refill amount.
     */
    private void refillTokens() {
        int sinceLastRefillMilliseconds = Instant.now().compareTo(lastRefill);
        long tokensToRefill = (long) (sinceLastRefillMilliseconds / refillIntervalMilliseconds) * refillAmount;
        if (tokensToRefill > 0) {
            buckets.forEach(
                    (resourceKey, tokenCount) -> tokenCount = Math.min(maximumTokens, tokenCount + tokensToRefill));
        }
    }

    /**
     * Checks whether a resource can be obtained using the specified resourceKey. If
     * the resource is obtainable, the corresponding bucket has its tokens
     * decremented by the specified tokenDecrement amount.
     * 
     * @param resourceKey    The key indicating the resource trying to be acquired.
     * @param tokenDecrement The number of tokens to decrement from the bucket for
     *                       that resource.
     * @return A boolean determining whether the resource can be acquired (true if
     *         it can be acquired, false if it cannot).
     */
    public boolean tryGetResource(String resourceKey, long tokenDecrement) {
        refillTokens();

        buckets.putIfAbsent(resourceKey, maximumTokens);
        long resourceCurrentTokens = buckets.get(resourceKey);
        if (buckets.get(resourceKey) >= tokenDecrement) {
            buckets.put(resourceKey, resourceCurrentTokens - tokenDecrement);
            return true;
        }
        return false;
    }
}
