package com.thai.finance.api.finance.api.respository;

import com.thai.finance.api.finance.api.entities.Stock_Movement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockMovementRepository  extends JpaRepository<Stock_Movement, UUID> {
}
