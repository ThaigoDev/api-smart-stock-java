package com.thai.finance.api.finance.api.respository;

import com.thai.finance.api.finance.api.entities.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockRespository  extends JpaRepository<Stock, UUID> {
}
