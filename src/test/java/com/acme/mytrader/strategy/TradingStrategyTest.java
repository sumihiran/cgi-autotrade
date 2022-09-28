package com.acme.mytrader.strategy;

import com.acme.mytrader.price.PriceSource;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TradingStrategyTest {
    @Test
    public void tradingStrategySubscribeToPriceListener() {
        var priceSource = spy(PriceSource.class);
        try (var tradingStrategy = new TradingStrategy("IBM")) {
            assertNull(tradingStrategy.getPriceSource());

            tradingStrategy.setPriceSource(priceSource);

            verify(priceSource, times(1)).addPriceListener(tradingStrategy);
        }
        verify(priceSource, times(1)).removePriceListener(any());
    }

    @Test
    public void monitorStockPriceWhenPriceUpdated() {
        // Arrange
        var tradingStrategy = new TradingStrategy("IBM");

        tradingStrategy.priceUpdate("IBM", 121.74);
        assertEquals(Optional.of(121.74), tradingStrategy.getStockPrice());

        tradingStrategy.priceUpdate("IBM", 120.00);
        tradingStrategy.priceUpdate("APL", 151.76);

        assertEquals(Optional.of(120.00), tradingStrategy.getStockPrice());
    }
}
