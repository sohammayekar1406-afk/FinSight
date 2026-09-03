package com.ledgerlens.repository;
import com.ledgerlens.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface MerchantRepository extends JpaRepository<Merchant, UUID> { Optional<Merchant> findByMerchantIdAndActiveTrue(String merchantId); Optional<Merchant> findByMerchantId(String merchantId); }
