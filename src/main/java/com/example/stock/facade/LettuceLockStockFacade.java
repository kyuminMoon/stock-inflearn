package com.example.stock.facade;

import org.springframework.stereotype.Component;

import com.example.stock.repository.LockRepository;
import com.example.stock.repository.RedisLockRepository;
import com.example.stock.service.StockService;

@Component
public class LettuceLockStockFacade {
  private final RedisLockRepository lockRepository;

  private final StockService stockService;

  public LettuceLockStockFacade(RedisLockRepository lockRepository, StockService stockService) {
    this.lockRepository = lockRepository;
    this.stockService = stockService;
  }

  public void decrease(Long key, Long quantity) throws InterruptedException {
    while(!lockRepository.lock(key)){
      Thread.sleep(100);
    }

    try {
      stockService.decrease(key, quantity);
    } finally {
      lockRepository.unlock(key);
    }
  }
}