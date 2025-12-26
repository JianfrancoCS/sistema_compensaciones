package com.agropay.core.states.application;
import com.agropay.core.address.application.IAddressUseCase;
import com.agropay.core.address.application.IAddressableUseCase;
import com.agropay.core.address.domain.AddressEntity;
import com.agropay.core.address.persistence.IAddressRepository;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService implements IAddressUseCase {
    private final IAddressRepository addressRepository;

    @Override
    @Transactional
    public AddressEntity saveForEntity(IAddressableUseCase<?> entity, AddressEntity address) {
        // Si es dirección primaria, desmarcar las otras como primarias
        if (address.isPrimary()) {
            unsetPrimaryAddresses(entity);
        }

        address.setAddressableId(String.valueOf(entity.getId()));
        address.setAddressableType(entity.getAddressableType());

        return addressRepository.save(address);
    }

    @Override
    public Optional<AddressEntity> findPrimaryByEntity(IAddressableUseCase<?> entity) {
        return addressRepository.findByAddressableIdAndAddressableTypeAndIsPrimaryTrue(
                String.valueOf(entity.getId()),
                entity.getAddressableType()
        );
    }

    @Override
    public List<AddressEntity> findByEntity(IAddressableUseCase<?> entity) {
        return addressRepository.findByAddressableIdAndAddressableType(
                String.valueOf(entity.getId()),
                entity.getAddressableType()
        );
    }

    @Override
    public AddressEntity getByIdentifier(UUID identifier) {
        return addressRepository.findByPublicId(identifier)
                .orElseThrow(() -> new IdentifierNotFoundException(
                        String.format("Address with public id %s not found", identifier)
                ));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AddressEntity address = addressRepository.findById(id)
                .orElseThrow(() -> new IdentifierNotFoundException(
                        String.format("Address with id %s not found", id)
                ));

        // Realizar soft delete
        String currentUser = getCurrentUser();
       addressRepository.softDelete(id, LocalDateTime.now(), currentUser);
    }

    // Método para cambiar dirección primaria
    @Transactional
    public AddressEntity setPrimaryAddress(IAddressableUseCase<?> entity, UUID addressPublicId) {
        // Obtener la nueva dirección primaria
        AddressEntity newPrimaryAddress = getByIdentifier(addressPublicId);

        // Verificar que pertenece a la entidad correcta
        if (!newPrimaryAddress.getAddressableId().equals(String.valueOf(entity.getId())) ||
                !newPrimaryAddress.getAddressableType().equals(entity.getAddressableType())) {
            throw new IllegalArgumentException("Address does not belong to the specified entity");
        }

        // Desmarcar direcciones primarias actuales
        unsetPrimaryAddresses(entity);

        // Marcar la nueva como primaria
        newPrimaryAddress.setPrimary(true);
        return addressRepository.save(newPrimaryAddress);
    }

    // Método privado para desmarcar direcciones primarias
    private void unsetPrimaryAddresses(IAddressableUseCase<?> entity) {
        List<AddressEntity> addresses = findByEntity(entity);
        addresses.forEach(addr -> {
            if (addr.isPrimary()) {
                addr.setPrimary(false);
                addressRepository.save(addr);
            }
        });
    }

    // Método privado para obtener usuario actual
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system"; // fallback
    }

    /**
     * Actualizar una dirección existente
     */
    @Transactional
    public AddressEntity updateAddress(UUID publicId, AddressEntity updatedAddress) {
        AddressEntity existingAddress = getByIdentifier(publicId);

        // Si se está cambiando a primaria, desmarcar las otras
        if (updatedAddress.isPrimary() && !existingAddress.isPrimary()) {
            // Crear una entidad temporal para usar unsetPrimaryAddresses
            IAddressableUseCase<?> tempEntity = new IAddressableUseCase<String>() {
                @Override
                public String getId() {
                    return existingAddress.getAddressableId();
                }

                @Override
                public String getAddressableType() {
                    return existingAddress.getAddressableType();
                }
            };
            unsetPrimaryAddresses(tempEntity);
        }

        // Actualizar campos
        existingAddress.setLongitude(updatedAddress.getLongitude());
        existingAddress.setLatitude(updatedAddress.getLatitude());
        existingAddress.setPrimary(updatedAddress.isPrimary());

        return addressRepository.save(existingAddress);
    }
}