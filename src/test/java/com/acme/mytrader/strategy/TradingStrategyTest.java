package com.acme.mytrader.strategy;

import com.acme.mytrader.execution.ExecutionService;
import com.acme.mytrader.price.PriceSource;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TradingStrategyTest {
    private ExecutionService executionService;
    private PriceSource priceSource;

    @Before
    public void setUp() {
        executionService = spy(ExecutionService.class);
        priceSource = spy(PriceSource.class);
    }

    @Test
    public void tradingStrategySubscribeToPriceListener() {
        try (var tradingStrategy =
                     TradingStrategy.placeSellOrderForSecurityWhenPriceAbove("IBM", 100, 101, 10)) {
            assertNull(tradingStrategy.getPriceSource());

            tradingStrategy.subscribeAndExecute(priceSource, executionService);

            verify(priceSource, times(1)).addPriceListener(tradingStrategy);
            assertEquals(priceSource, tradingStrategy.getPriceSource());
            assertEquals(executionService, tradingStrategy.getExecutionService());
        }
        verify(priceSource, times(1)).removePriceListener(any());
    }

    @Test
    public void monitorStockPriceWhenPriceUpdated() {
        var tradingStrategy = TradingStrategy
                .placeSellOrderForSecurityWhenPriceAbove("IBM", 100, 101, 10);

        tradingStrategy.priceUpdate("IBM", 121.74);
        assertEquals(Optional.of(121.74), tradingStrategy.getStockPrice());

        tradingStrategy.priceUpdate("IBM", 120.00);
        tradingStrategy.priceUpdate("APL", 151.76);

        assertEquals(Optional.of(120.00), tradingStrategy.getStockPrice());
    }

    @Test
    public void OrderIsPlacedWhenTriggerLevelIsCrossed() {
        // Arrange
        var strategy = TradingStrategy
                .placeBuyOrderForSecurityWhenPriceAbove("IBM", 100, 101, 50);

        // Act
        strategy.subscribeAndExecute(priceSource, executionService);

        strategy.priceUpdate("IBM", 99.3);
        strategy.priceUpdate("IBM", 101.0);
        strategy.priceUpdate("IBM", 105.0);

        // Assert
        var resultStockPrice = strategy.getStockPrice();
        assertTrue(resultStockPrice.isPresent());
        assertEquals(Optional.of(105.0), resultStockPrice);
        verify(executionService, times(1)).buy("IBM", 101, 50);

        assertNull(strategy.getPriceSource());
        assertNull(strategy.getExecutionService());
    }

    @Test
    public void shouldNotPlaceOrderWhenTriggerLevelNotReached() {
        // Arrange
        var strategy = TradingStrategy
                .placeSellOrderForSecurityWhenPriceBelow("IBM", 100, 99, 10);

        // Act
        strategy.subscribeAndExecute(priceSource, executionService);

        strategy.priceUpdate("IBM", 101.10);
        strategy.priceUpdate("IBM", 105.0);

        // Assert
        var resultStockPrice = strategy.getStockPrice();
        assertEquals(Optional.of(105.0), resultStockPrice);
        verify(executionService, times(0)).sell("IBM", 99, 10);

        assertNotNull(strategy.getPriceSource());
        assertNotNull(strategy.getExecutionService());
    }
}
