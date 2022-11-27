package com.example.stock.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;

@SpringBootTest
class StockServiceTest {

  @Autowired
  private PessimisticLockStockService stockService;

  @Autowired
  private StockRepository stockRepository;

  @BeforeEach
  public void before() {
    Stock stock = new Stock(1L, 100L);
    stockRepository.saveAndFlush(stock);
  }

  @AfterEach
  public void after() {
    stockRepository.deleteAll();
  }

  @Test
  void stock_decrease() {
    stockService.decrease(1L, 1L);

    // 100 - 1 = 99
    Stock stock = stockRepository.findById(1L).orElseThrow();
    assertEquals(99, stock.getQuantity());
  }

  @Test
  public void 동시에_100개의_요청() throws InterruptedException {
    int threadCount = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(32);
    CountDownLatch latch = new CountDownLatch(threadCount);
    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          stockService.decrease(1L, 1L);
        } finally {
          latch.countDown();
        }
      });
    }
    latch.await();

    Stock stock = stockRepository.findById(1L).orElseThrow();

    // 100-(1*100) = 0
    // 레이스 컨디션 이슈로 테스트 실패
    assertEquals(0L, stock.getQuantity());


  }
}