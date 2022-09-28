# Developer Programming Exercise

## User Story

As a trader I want to be able to monitor stock prices such that when they breach a trigger level orders can be executed automatically.

Note:

The implementation of PriceSource and EecutionService is out of scope, assuming that it will be provided by third party.
You need to listen to price updates from PriceSource and act accordingly.

## Exercise

Given the following interface definitions (provided)

```
public interface ExecutionService {
    void buy(String security, double price, int volume);
    void sell(String security, double price, int volume);
}
```

```
public interface PriceListener {
    void priceUpdate(String security, double price);
}
```

```
public interface PriceSource {
    void addPriceListener(PriceListener listener);
    void removePriceListener(PriceListener listener);
}
```

Develop a basic implementation of the PriceListener interface that provides the following behaviour:

1. Monitors price movements on a specified single stock (e.g. "IBM")
1. Executes a single "buy" instruction for a specified number of lots (e.g. 100) as soon as the price of that stock is seen to be below
a specified price (e.g. 55.0). Don’t worry what units that is in.

### Considerations

* Please "work out loud" and ask questions
* This is not a test of your API knowledge so feel free to check the web as reference
* There is no specific solution we are looking for

### Some libraries already available:

* Java 8
* JUnit 4
* Mockito
* EasyMock
* JMock

### Sample

Executes a single "buy" instruction for a specified number of lots (e.g. 100) as soon as the price of that stock is seen to be below
a specified price (e.g. 55.0). Don’t worry what units that is in.

Assumption: Trigger level price (e.g. 55.0) may not be equivalent to the order price

```
ExecutionService executionService = ...
priceSource PriceSource = ...

var buyStrategy = TradingStrategy
    .placeBuyOrderForSecurityWhenPriceBelow("IBM", 55.0, 54.0, 100);
    
buyStrategy.setExecutionService(executionService);
buyStrategy.setPriceSource(PriceSource); // subscribe to the observable

buyStrategy.close(); // unsubscribe

```
