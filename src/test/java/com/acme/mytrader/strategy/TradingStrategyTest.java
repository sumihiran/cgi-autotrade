package com.acme.mytrader.strategy;

import com.acme.mytrader.execution.ExecutionService;
import com.acme.mytrader.price.PriceSource;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TradingStrategyTest {
    @Test
    public void tradingStrategySubscribeToPriceListener() {
        var priceSource = spy(PriceSource.class);
        try (var tradingStrategy =
                     TradingStrategy.placeSellOrderForSecurityWhenPriceAbove("IBM", 100, 101, 10)) {
            assertNull(tradingStrategy.getPriceSource());

            tradingStrategy.setPriceSource(priceSource);

            verify(priceSource, times(1)).addPriceListener(tradingStrategy);
        }
        verify(priceSource, times(1)).removePriceListener(any());
    }

    @Test
    public void monitorStockPriceWhenPriceUpdated() {
        // Arrange
        var tradingStrategy = TradingStrategy
                .placeSellOrderForSecurityWhenPriceAbove("IBM", 100, 101, 10);

        tradingStrategy.priceUpdate("IBM", 121.74);
        assertEquals(Optional.of(121.74), tradingStrategy.getStockPrice());

        tradingStrategy.priceUpdate("IBM", 120.00);
        tradingStrategy.priceUpdate("APL", 151.76);

        assertEquals(Optional.of(120.00), tradingStrategy.getStockPrice());
    }

    @Test
    public void shouldTriggerOrderWhenTriggerLevelReached()
    {
        var strategy = TradingStrategy
                .placeBuyOrderForSecurityWhenPriceAbove("IBM", 100, 101,50);
        var executionService = spy(ExecutionService.class);
        strategy.setExecutionService(executionService);

        // Act
        strategy.priceUpdate("IBM", 101.0);
        strategy.priceUpdate("IBM", 105.0);

        // Assert
        assertEquals(executionService, strategy.getExecutionService().orElse(null));

        var resultStockPrice = strategy.getStockPrice();
        assertTrue(resultStockPrice.isPresent());
        assertEquals(Optional.of(105.0), resultStockPrice);
        verify(executionService, times(1)).buy("IBM", 101, 50);
    }

    @Test
    public void shouldNotTriggerOrderWhenTriggerLevelNotReached()
    {
        var strategy = TradingStrategy
                .placeSellOrderForSecurityWhenPriceBelow("IBM", 100, 99,10);
        var executionService = spy(ExecutionService.class);
        strategy.setExecutionService(executionService);

        // Act
        strategy.priceUpdate("IBM", 100.0);
        strategy.priceUpdate("IBM", 105.0);

        // Assert
        var resultStockPrice = strategy.getStockPrice();
        assertEquals(Optional.of(105.0), resultStockPrice);
        verify(executionService, times(0)).sell("IBM", 99, 10);
    }

    @Test
    public void shouldTriggerOrderWhenTriggerLevelReachedTwice()
    {
        var sellStrategy = TradingStrategy
                .placeSellOrderForSecurityWhenPriceAbove("CGI", 50, 52,10);
        var buyStrategy = TradingStrategy
                .placeBuyOrderForSecurityWhenPriceBelow("CGI", 45, 44,100);

        var executionService = spy(ExecutionService.class);
        sellStrategy.setExecutionService(executionService);
        buyStrategy.setExecutionService(executionService);

        var prices = Arrays.asList(40, 44, 51, 48, 42, 48, 52, 55, 46);

        // Act
        prices.forEach(price -> {
            sellStrategy.priceUpdate("CGI", price);
            buyStrategy.priceUpdate("CGI", price);
        });

        // Assert
        assertEquals(Optional.of(46.0), sellStrategy.getStockPrice());
        assertEquals(Optional.of(46.0), buyStrategy.getStockPrice());

        verify(executionService, times(2)).sell("CGI", 52, 10);
        verify(executionService, times(2)).buy("CGI", 44, 100);
    }
}
