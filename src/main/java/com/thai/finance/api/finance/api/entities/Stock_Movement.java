package com.thai.finance.api.finance.api.entities;

import com.thai.finance.api.finance.api.enums.MovementType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "TB_STOCK_MOVEMENT")
public class Stock_Movement {

    @Id
    @GeneratedValue(strategy  = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    private MovementType type;

    @Column(name= "QUANTITY_MOVEMENT")
    private Integer quantityMovement;

    private Instant createAt = Instant.now();
    private Instant updateAt= null;

    public Stock_Movement() {
    }

    public Stock_Movement(UUID id, Product product, MovementType type, Integer quantityMovement) {
        this.id = id;
        this.product = product;
        this.type = type;
        this.quantityMovement = quantityMovement;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public Integer getQuantityMovement() {
        return quantityMovement;
    }

    public void setQuantityMovement(Integer quantityMovement) {
        this.quantityMovement = quantityMovement;
    }

    public Instant getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Instant createAt) {
        this.createAt = createAt;
    }

    public Instant getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Instant updateAt) {
        this.updateAt = updateAt;
    }
}
