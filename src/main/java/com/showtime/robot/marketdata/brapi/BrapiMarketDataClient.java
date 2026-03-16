package com.showtime.robot.marketdata.brapi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.showtime.robot.domain.Candle;
import com.showtime.robot.marketdata.MarketDataClient;
import com.showtime.robot.marketdata.MarketDataRange;
import com.showtime.robot.marketdata.brapi.dto.BrapiQuoteResponse;
import com.showtime.robot.marketdata.brapi.dto.BrapiQuoteResult;
import com.showtime.robot.marketdata.brapi.dto.BrapiQuoteResultHistorical;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BrapiMarketDataClient implements MarketDataClient {
  private static final Logger log = LoggerFactory.getLogger(BrapiMarketDataClient.class);

  private final HttpClient http;
  private final ObjectMapper mapper;
  private final String baseUrl;
  private final Optional<String> token;

  public BrapiMarketDataClient() {
    this(
        HttpClient.newHttpClient(),
        defaultMapper(),
        "https://brapi.dev",
        Optional.ofNullable(System.getenv("BRAPI_TOKEN")).filter(s -> !s.isBlank()));
  }

  public BrapiMarketDataClient(
      HttpClient http, ObjectMapper mapper, String baseUrl, Optional<String> token) {
    this.http = Objects.requireNonNull(http);
    this.mapper = Objects.requireNonNull(mapper);
    this.baseUrl = Objects.requireNonNull(baseUrl);
    this.token = Objects.requireNonNull(token);
  }

  @Override
  public List<Candle> getDailyCandles(String symbol, MarketDataRange range) {
    String s = requireSymbol(symbol);
    URI uri = buildUri(s, range);

    HttpRequest req =
        HttpRequest.newBuilder().uri(uri).GET().header("Accept", "application/json").build();

    log.info("Fetching candles: symbol={} range={} url={}", s, range.apiValue(), uri);
    String body;
    try {
      HttpResponse<String> resp =
          http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
        throw new RuntimeException("BRAPI HTTP " + resp.statusCode() + ": " + resp.body());
      }
      body = resp.body();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("Failed to call brapi", e);
    }

    BrapiQuoteResponse parsed;
    try {
      parsed = mapper.readValue(body, BrapiQuoteResponse.class);
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse brapi response", e);
    }

    if (parsed.results() == null || parsed.results().isEmpty()) {
      throw new RuntimeException("BRAPI response missing results");
    }
    BrapiQuoteResult r = parsed.results().getFirst();
    if (r.historicalDataPrice() == null || r.historicalDataPrice().isEmpty()) {
      throw new RuntimeException("BRAPI response missing historicalDataPrice");
    }

    return r.historicalDataPrice().stream()
        .filter(
            h ->
                h.date() != null
                    && h.open() != null
                    && h.high() != null
                    && h.low() != null
                    && h.close() != null)
        .map(BrapiMarketDataClient::toCandle)
        .sorted(Comparator.comparing(Candle::time))
        .toList();
  }

  private static Candle toCandle(BrapiQuoteResultHistorical h) {
    Instant time = Instant.ofEpochSecond(h.date());
    return new Candle(
        time,
        BigDecimal.valueOf(h.open()),
        BigDecimal.valueOf(h.high()),
        BigDecimal.valueOf(h.low()),
        BigDecimal.valueOf(h.close()),
        h.volume() == null ? 0L : h.volume(),
        h.adjustedClose() == null
            ? BigDecimal.valueOf(h.close())
            : BigDecimal.valueOf(h.adjustedClose()));
  }

  private URI buildUri(String symbol, MarketDataRange range) {
    String encodedSymbol = URLEncoder.encode(symbol, StandardCharsets.UTF_8);
    StringBuilder qs = new StringBuilder();
    qs.append("range=").append(range.apiValue());
    qs.append("&interval=1d");
    token.ifPresent(t -> qs.append("&token=").append(URLEncoder.encode(t, StandardCharsets.UTF_8)));
    return URI.create(baseUrl + "/api/quote/" + encodedSymbol + "?" + qs);
  }

  private static String requireSymbol(String symbol) {
    if (symbol == null || symbol.isBlank()) {
      throw new IllegalArgumentException("symbol is required (example: PETR4)");
    }
    return symbol.trim().toUpperCase();
  }

  private static ObjectMapper defaultMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }
}
