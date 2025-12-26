package com.agropay.core.address.persistence;

import com.agropay.core.address.domain.AddressEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IAddressRepository extends ISoftRepository<AddressEntity, Long> {

    // Consultas normales (automáticamente filtran deleted_at IS NULL por @SQLRestriction)
    List<AddressEntity> findByAddressableIdAndAddressableType(String addressableId, String addressableType);

    Optional<AddressEntity> findByAddressableIdAndAddressableTypeAndIsPrimaryTrue(String addressableId, String addressableType);

    Optional<AddressEntity> findByPublicId(UUID publicId);

    // Consultas que incluyen registros eliminados (para casos especiales)
    @Query("SELECT a FROM AddressEntity a WHERE a.addressableId = :addressableId AND a.addressableType = :addressableType")
    List<AddressEntity> findByAddressableIdAndAddressableTypeIncludingDeleted(
            @Param("addressableId") String addressableId,
            @Param("addressableType") String addressableType
    );


}