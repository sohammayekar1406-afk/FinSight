package com.ledgerlens.repository;
import com.ledgerlens.entity.MerchantSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface MerchantSettingsRepository extends JpaRepository<MerchantSettings, UUID> { Optional<MerchantSettings> findByMerchant_MerchantId(String merchantId); }
