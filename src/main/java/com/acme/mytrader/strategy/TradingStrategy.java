package com.acme.mytrader.strategy;

import com.acme.mytrader.execution.ExecutionService;
import com.acme.mytrader.price.PriceListener;
import com.acme.mytrader.price.PriceSource;

import java.util.Optional;

/**
 * A {@link PriceSource} observer that monitors the stock price of its security and executes orders automatically when
 * the stock prices breach a particular trigger level.
 *
 * <pre>
 * User Story: As a trader I want to be able to monitor stock prices such
 * that when they breach a trigger level orders can be executed automatically
 * </pre>
 *
 * @author Nuwan Sumihiran
 */
public class TradingStrategy implements PriceListener, AutoCloseable {
    private static final String BUY = "buy";
    private static final String SELL = "sell";
    private static final String DIRECTION_HIGH = "high";
    private static final String DIRECTION_LOW = "low";

    private final String security;

    private final double triggerLevelPrice;
    private final String triggerDirection;
    private final String orderType;
    private final double orderPrice;
    private final int orderVolume;

    private boolean triggerLevelCrossed = false;
    private double stockPrice;
    private PriceSource priceSource;
    private ExecutionService executionService;

    /**
     * Initialize a {@link TradingStrategy} for the given {@code security} to place a BUY order when the stock price
     * drops below the given {@code triggerLevelPrice}.
     *
     * @param security          the name of the security
     * @param triggerLevelPrice the price level to check against
     * @param buyPrice          the buy order price
     * @param buyVolume         the buy order volume
     * @return an initialized {@link TradingStrategy} instance
     */
    public static TradingStrategy placeBuyOrderForSecurityWhenPriceBelow(
            String security, double triggerLevelPrice, double buyPrice, int buyVolume) {
        return new TradingStrategy(
                security, triggerLevelPrice, DIRECTION_LOW, BUY, buyPrice, buyVolume);
    }

    /**
     * Initialize a {@link TradingStrategy} for the given {@code security} to place a BUY order when the stock price
     * rise above the given {@code triggerLevelPrice}.
     *
     * @param security          the name of the security
     * @param triggerLevelPrice the price level to check against
     * @param buyPrice          the buy order price
     * @param buyVolume         the buy order volume
     * @return an initialized {@link TradingStrategy} instance
     */
    public static TradingStrategy placeBuyOrderForSecurityWhenPriceAbove(
            String security, double triggerLevelPrice, double buyPrice, int buyVolume) {
        return new TradingStrategy(
                security, triggerLevelPrice, DIRECTION_HIGH, BUY, buyPrice, buyVolume);
    }

    /**
     * Initialize a {@link TradingStrategy} for the given {@code security} to place a SELL order when the stock price
     * drops below the given {@code triggerLevelPrice}.
     *
     * @param security          the name of the security
     * @param triggerLevelPrice the price level to check against
     * @param sellPrice         the sell order price
     * @param sellVolume        the sell order volume
     * @return an initialized {@link TradingStrategy} instance
     */
    public static TradingStrategy placeSellOrderForSecurityWhenPriceBelow(
            String security, double triggerLevelPrice, double sellPrice, int sellVolume) {
        return new TradingStrategy(
                security, triggerLevelPrice, DIRECTION_LOW, SELL, sellPrice, sellVolume);
    }

    /**
     * Initialize a {@link TradingStrategy} for the given {@code security} to place a SELL order when the stock price
     * rise above the given {@code triggerLevelPrice}.
     *
     * @param security          the name of the security
     * @param triggerLevelPrice the price level to check against
     * @param sellPrice         the sell order price
     * @param sellVolume        the sell order volume
     * @return an initialized {@link TradingStrategy} instance
     */
    public static TradingStrategy placeSellOrderForSecurityWhenPriceAbove(
            String security, double triggerLevelPrice, double sellPrice, int sellVolume) {
        return new TradingStrategy(
                security, triggerLevelPrice, DIRECTION_HIGH, SELL, sellPrice, sellVolume);
    }

    /**
     * Initialize a {@link TradingStrategy} with the given {@code security}, {@code triggerLevelPrice},
     * {@code triggerDirection} and executes an order of {@code orderType} with given {@code orderPrice}
     * and {@code orderVolume}
     *
     * @param security          the name of the security
     * @param triggerLevelPrice the price level to trigger an order
     * @param triggerDirection  the direction of trigger. either {@code DIRECTION_HIGH} or @{code DIRECTION_LOW}
     * @param orderType         the type of the order. either {@code BUY} or {@code SELL}
     * @param orderPrice        the price of the placing order
     * @param orderVolume       the volume of the placing order
     */
    protected TradingStrategy(
            String security, double triggerLevelPrice, String triggerDirection,
            String orderType, double orderPrice, int orderVolume) {
        this.security = security;
        this.triggerLevelPrice = triggerLevelPrice;
        this.triggerDirection = triggerDirection;
        this.orderType = orderType;
        this.orderPrice = orderPrice;
        this.orderVolume = orderVolume;
    }

    @Override
    public void priceUpdate(String security, double price) {
        if (security.equals(this.security)) {
            this.onPriceUpdate(price);
        }
    }

    private void onPriceUpdate(double price) {
        this.stockPrice = price;

        if (shouldTrigger(price)) {
            executeOrder();
            triggerLevelCrossed = true;
        } else if (!isTriggerLevelSatisfied(price) && triggerLevelCrossed) {
            triggerLevelCrossed = false;
        }
    }

    private void executeOrder() {
        if (executionService == null) {
            return;
        }

        if (orderType.equals(BUY)) {
            executionService.buy(security, orderPrice, orderVolume);
        } else if (orderType.equals(SELL)) {
            executionService.sell(security, orderPrice, orderVolume);
        }
    }

    private boolean shouldTrigger(double price) {
        return isTriggerLevelSatisfied(price) && !triggerLevelCrossed;
    }

    private boolean isTriggerLevelSatisfied(double price) {
        if (triggerDirection.equals(DIRECTION_HIGH)) {
            return price > triggerLevelPrice;
        }

        return price < triggerLevelPrice;
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
     * Returns the price level to trigger an order
     *
     * @return price level to trigger an order
     */
    public double getTriggerLevelPrice() {
        return triggerLevelPrice;
    }

    /**
     * Returns the direction of trigger
     *
     * @return either {@code DIRECTION_HIGH} or @{code DIRECTION_LOW}
     */
    public String getTriggerDirection() {
        return triggerDirection;
    }

    /**
     * Returns the type of the order to be executed
     *
     * @return the type of the order. either {@code BUY} or {@code SELL}
     */
    public String getOrderType() {
        return orderType;
    }

    /***
     * Returns the price of the placing order
     *
     * @return the price of the placing order
     */
    public double getOrderPrice() {
        return orderPrice;
    }

    /**
     * Returns the volume of the placing order
     *
     * @return the volume of the placing order
     */
    public int getOrderVolume() {
        return orderVolume;
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

    /**
     * Gets the {@link ExecutionService} in use
     *
     * @return an optional of {@link ExecutionService}
     */
    public Optional<ExecutionService> getExecutionService() {
        return Optional.of(executionService);
    }

    /**
     * Sets the order {@code ExecutionService}
     *
     * @param executionService the {@code ExecutionService}
     */
    public void setExecutionService(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @Override
    public void close() {
        if (this.priceSource != null) {
            this.priceSource.removePriceListener(this);
        }
    }
}
