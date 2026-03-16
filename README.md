## ShowTime

Java-based trading research and automation project focused on Brazilian stocks (via `brapi.dev`) with an extensible architecture for multi-timeframe strategies, backtesting, and, later, live trading.

### Overview

- **Market data**: `BrapiMarketDataClient` fetches daily candles from `brapi.dev`, returning a unified `Candle` domain object.
- **Trading core** (package `com.showtime.robot.trading`):
  - `Strategy` interface receives a `MultiTimeframeSnapshot` (daily/weekly/monthly candles) and a `StrategyContext` (cash and open position) and returns a `StrategyDecision` (buy/sell/hold, position size, optional stop loss / take profit).
  - `BacktestEngine` runs a `Strategy` over historical candles using a `SimulatedBroker`, producing a `BacktestResult` with equity and performance metrics.
- **Execution**:
  - `SimulatedBroker` tracks cash, positions, and applies trades for backtests.
  - A real broker implementation can be added later for live trading, using the same `Strategy` interface.

### Project structure (key packages)

- `com.showtime.robot.domain`
  - `Candle` – immutable OHLCV candle used across the project.
- `com.showtime.robot.marketdata`
  - `MarketDataClient` – abstraction for historical candles.
  - `BrapiMarketDataClient` – implementation using `brapi.dev`.
- `com.showtime.robot.trading`
  - `Strategy`, `StrategyDecision`, `StrategyContext`
  - `MultiTimeframeSnapshot`, `Timeframe`
- `com.showtime.robot.trading.broker`
  - `SimulatedBroker`, `Broker`, `AccountState`, `Position`
- `com.showtime.robot.trading.backtest`
  - `BacktestEngine`, `BacktestResult`
- `com.showtime.robot.trading.strategies`
  - `DailySmaCrossStrategy` – example daily SMA crossover strategy.
- `com.showtime.robot.trading.app`
  - `BacktestRunner` – command line entrypoint to run backtests.

### Running a backtest

Make sure you have:

- Java 17+ installed.
- Optional: environment variable `BRAPI_TOKEN` set, if you use a token for `brapi.dev`.

From the project root:

```bash
mvn clean package
```

Then run the backtest (example with PETR4 over the last 2 years):

```bash
java -cp target/ShowTime-*.jar com.showtime.robot.trading.app.BacktestRunner PETR4 2y
```

CLI arguments:

- **SYMBOL**: stock ticker understood by `brapi.dev` (for example `PETR4`, `VALE3`).
- **RANGE**: history range using the same format as `brapi.dev`/Yahoo (`5d`, `1mo`, `3mo`, `6mo`, `1y`, `2y`, `5y`, `10y`, `max`).

The runner prints:

- Initial cash
- Final cash
- Equity (cash + open positions marked to last price)
- Number of trades
- Total return percentage

### Extending the project

- **Multi-timeframe strategies**: `MultiTimeframeSnapshot` already supports daily, weekly, and monthly candles. Extend `BacktestEngine` to load weekly/monthly data and populate the snapshot, then adapt your strategies to use all three.
- **New strategies**: Implement the `Strategy` interface and plug it into `BacktestRunner` or your own runner.
- **Live trading**: Implement a `Broker` that talks to your real broker API and wire a `LiveEngine` that feeds candles and executes `StrategyDecision` in real time.

# Investment Robot (Java)

Swing-trade oriented investment robot starter project:

- **Market data (free)**: daily OHLCV candles from `brapi.dev`
- **Analysis**: basic indicators + simple support/resistance detection
- **Broker integration (EU-ready)**: clean `BrokerClient` interface + **paper broker** implementation (safe by default)

## Requirements

- Java **21+** installed (`java -version`)

This repo includes a helper script that downloads a portable Maven into `.tools/` so you can build without installing Maven system-wide.

## Quickstart (Windows / PowerShell)

```powershell
cd C:\HORA_DO_SHOW\Robot
.\scripts\setup-maven.ps1
.\scripts\run.ps1 candles --symbol PETR4 --range 1y
.\scripts\run.ps1 analyze --symbol PETR4 --range 1y
```

## What the API returns

For `PETR4`, we call:

- `https://brapi.dev/api/quote/PETR4?range=1y&interval=1d`

And parse `results[0].historicalDataPrice[]` which includes:

- `date` (epoch seconds), `open`, `high`, `low`, `close`, `volume`, `adjustedClose`

## Roadmap (next steps)

- Add backtesting loop with position sizing + fees/slippage
- Strategy: swing entries at supports/resistances with trend filter (MA) + momentum (RSI/MACD)
- Add a real broker adapter (ex: Interactive Brokers Client Portal / FIX) behind `BrokerClient`

