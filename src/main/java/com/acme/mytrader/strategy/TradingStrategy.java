package com.acme.mytrader.strategy;

import com.acme.mytrader.price.PriceListener;
import com.acme.mytrader.price.PriceSource;

import java.util.Optional;

/**
 * A {@link PriceSource} observer that monitors the stock price of its security.
 *
 * <pre>
 * User Story: As a trader I want to be able to monitor stock prices such
 * that when they breach a trigger level orders can be executed automatically
 * </pre>
 *
 * @author Nuwan Sumihiran
 */
public class TradingStrategy implements PriceListener, AutoCloseable {

    private final String security;
    private double stockPrice;
    private PriceSource priceSource;

    /**
     * Initialize a {@link TradingStrategy} with the given {@code security} name
     *
     * @param security the name of the stock
     */
    public TradingStrategy(String security) {
        this.security = security;
    }

    @Override
    public void priceUpdate(String security, double price) {
        if (security.equals(this.security)) {
            this.stockPrice = price;
        }
    }

    /**
     * Returns security name
     *
     * @return the name of the security
     */
    public String getSecurity() {
        return security;
    }

    /**
     * Returns observed stock price
     *
     * @return current stock price
     */
    public Optional<Double> getStockPrice() {
        return Optional.of(stockPrice);
    }

    /**
     * Returns the {@link PriceSource} currently subscribed to
     *
     * @return the subscribed {@link PriceSource}
     */
    public PriceSource getPriceSource() {
        return priceSource;
    }

    /**
     * Subscribe this strategy to the given {@link PriceSource}. If a {@link PriceSource} already subscribed it will
     * be unsubscribed.
     *
     * @param priceSource a {@link PriceSource} to subscribe
     */
    public void setPriceSource(PriceSource priceSource) {
        if (this.priceSource != null) {
            this.close();
        }
        priceSource.addPriceListener(this);
        this.priceSource = priceSource;
    }

    @Override
    public void close() {
        if (this.priceSource != null) {
            this.priceSource.removePriceListener(this);
        }
    }
}
