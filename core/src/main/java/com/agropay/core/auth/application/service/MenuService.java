package com.agropay.core.auth.application.service;

import com.agropay.core.auth.domain.ContainerEntity;
import com.agropay.core.auth.domain.ElementEntity;
import com.agropay.core.auth.domain.UserEntity;
import com.agropay.core.auth.model.NavigationItemDTO;
import com.agropay.core.auth.persistence.IContainerRepository;
import com.agropay.core.auth.persistence.IElementRepository;
import com.agropay.core.auth.persistence.IUserProfileRepository;
import com.agropay.core.auth.persistence.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final IContainerRepository containerRepository;
    private final IElementRepository elementRepository;
    private final IUserRepository userRepository;
    private final IUserProfileRepository userProfileRepository;

    /**
     * Obtiene el menú basado en un perfil específico (método legacy, mantiene compatibilidad)
     */
    @Transactional(readOnly = true)
    public List<NavigationItemDTO> getMenuByProfileId(Short profileId) {
        log.debug("Building menu for profile ID: {}", profileId);
        return buildMenuFromProfileIds(Collections.singletonList(profileId), "WEB");
    }

    /**
     * Obtiene el menú basado en un usuario, unificando elementos de todos sus perfiles activos
     * Usa un Set para evitar duplicados cuando múltiples perfiles tienen los mismos elementos
     * 
     * @param userId ID del usuario
     * @param platform Plataforma solicitante: "WEB", "MOBILE", "DESKTOP" (opcional, por defecto "WEB")
     */
    @Transactional(readOnly = true)
    public List<NavigationItemDTO> getMenuByUserId(Short userId, String platform) {
        log.debug("Building menu for user ID: {} on platform: {}", userId, platform);

        // Normalizar plataforma (por defecto WEB)
        if (platform == null || platform.isBlank()) {
            platform = "WEB";
        }
        platform = platform.toUpperCase();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        List<Short> profileIds = new ArrayList<>();

        // Agregar perfil principal si existe
        if (user.getProfileId() != null) {
            profileIds.add(user.getProfileId());
        }

        // Agregar perfiles adicionales activos
        userProfileRepository.findActiveProfilesByUserId(userId).forEach(userProfile -> {
            Short additionalProfileId = userProfile.getProfile().getId();
            // Evitar duplicar el perfil principal si ya está en la lista
            if (!profileIds.contains(additionalProfileId)) {
                profileIds.add(additionalProfileId);
            }
        });

        if (profileIds.isEmpty()) {
            log.warn("User {} has no profiles assigned, returning empty menu", userId);
            return Collections.emptyList();
        }

        log.debug("User {} has {} profile(s): {}", userId, profileIds.size(), profileIds);
        return buildMenuFromProfileIds(profileIds, platform);
    }
    
    /**
     * Método legacy para compatibilidad (asume WEB)
     */
    @Transactional(readOnly = true)
    public List<NavigationItemDTO> getMenuByUserId(Short userId) {
        return getMenuByUserId(userId, "WEB");
    }

    /**
     * Construye el menú unificando elementos de múltiples perfiles usando Set para evitar duplicados
     * Filtra por plataforma si se especifica
     */
    private List<NavigationItemDTO> buildMenuFromProfileIds(List<Short> profileIds, String platform) {
        // Usar Map para almacenar elementos únicos por su ID (evita duplicados)
        Map<Short, ElementEntity> uniqueElementsMap = new HashMap<>();
        Map<Short, ContainerEntity> uniqueContainersMap = new HashMap<>();

        // Recorrer todos los perfiles y agregar elementos únicos
        for (Short profileId : profileIds) {
            // Elementos sin contenedor (filtrados por plataforma)
            List<ElementEntity> directElements = elementRepository.findElementsByProfileIdAndContainerIdAndPlatform(
                    profileId, null, platform);
            directElements.forEach(element -> {
                // Verificar que el elemento esté disponible en la plataforma
                if (isElementAvailableForPlatform(element, platform)) {
                    uniqueElementsMap.putIfAbsent(element.getId(), element);
                }
            });

            // Contenedores (filtrados por plataforma)
            List<ContainerEntity> containers = containerRepository.findContainersByProfileIdAndPlatform(profileId, platform);
            containers.forEach(container -> {
                // Verificar que el contenedor esté disponible en la plataforma
                if (isContainerAvailableForPlatform(container, platform)) {
                    uniqueContainersMap.putIfAbsent(container.getId(), container);
                }
            });

            // Elementos dentro de contenedores (filtrados por plataforma)
            for (ContainerEntity container : uniqueContainersMap.values()) {
                List<ElementEntity> containerElements = elementRepository.findElementsByProfileIdAndContainerIdAndPlatform(
                        profileId, container.getId(), platform);
                containerElements.forEach(element -> {
                    if (isElementAvailableForPlatform(element, platform)) {
                        uniqueElementsMap.putIfAbsent(element.getId(), element);
                    }
                });
            }
        }

        // Convertir Maps a Lists para procesamiento
        List<ElementEntity> uniqueElements = new ArrayList<>(uniqueElementsMap.values());
        List<ContainerEntity> uniqueContainers = new ArrayList<>(uniqueContainersMap.values());

        List<NavigationItemDTO> menuItems = new ArrayList<>();

        // Agregar elementos directos (sin contenedor)
        uniqueElements.stream()
                .filter(element -> element.getContainer() == null)
                .sorted(Comparator.comparing(ElementEntity::getOrderIndex))
                .forEach(element -> {
                    menuItems.add(new NavigationItemDTO(
                            element.getPublicId().toString(),
                            element.getDisplayName(),
                            element.getIcon() != null ? element.getIcon() : "",
                            element.getIconUrl() != null ? element.getIconUrl() : null,
                            element.getRoute() != null ? element.getRoute() : null
                    ));
                });

        // Agregar contenedores con sus elementos
        uniqueContainers.stream()
                .sorted(Comparator.comparing(ContainerEntity::getOrderIndex))
                .forEach(container -> {
                    // Obtener elementos de este contenedor (ya únicos por ID)
                    List<ElementEntity> containerElements = uniqueElements.stream()
                            .filter(element -> element.getContainer() != null &&
                                    element.getContainer().getId().equals(container.getId()))
                            .collect(Collectors.toList());

                    List<NavigationItemDTO> children = containerElements.stream()
                            .sorted(Comparator.comparing(ElementEntity::getOrderIndex))
                            .map(element -> new NavigationItemDTO(
                                    element.getPublicId().toString(),
                                    element.getDisplayName(),
                                    element.getIcon() != null ? element.getIcon() : "",
                                    element.getIconUrl() != null ? element.getIconUrl() : null,
                                    element.getRoute() != null ? element.getRoute() : null
                            ))
                            .collect(Collectors.toList());

                    NavigationItemDTO containerItem = new NavigationItemDTO(
                            container.getPublicId().toString(),
                            container.getDisplayName(),
                            container.getIcon() != null ? container.getIcon() : "",
                            container.getIconUrl() != null ? container.getIconUrl() : null,
                            null, // Los contenedores no tienen ruta directa
                            children
                    );

                    menuItems.add(containerItem);
                });

        log.debug("Menu built with {} items from {} profile(s) for platform {}", menuItems.size(), profileIds.size(), platform);
        return menuItems;
    }
    
    /**
     * Verifica si un elemento está disponible para la plataforma especificada
     */
    private boolean isElementAvailableForPlatform(ElementEntity element, String platform) {
        if (platform == null || platform.isBlank()) {
            return true; // Si no se especifica plataforma, mostrar todo
        }
        return switch (platform.toUpperCase()) {
            case "WEB" -> Boolean.TRUE.equals(element.getIsWeb());
            case "MOBILE" -> Boolean.TRUE.equals(element.getIsMobile());
            case "DESKTOP" -> Boolean.TRUE.equals(element.getIsDesktop());
            default -> true;
        };
    }
    
    /**
     * Verifica si un contenedor está disponible para la plataforma especificada
     */
    private boolean isContainerAvailableForPlatform(ContainerEntity container, String platform) {
        if (platform == null || platform.isBlank()) {
            return true; // Si no se especifica plataforma, mostrar todo
        }
        return switch (platform.toUpperCase()) {
            case "WEB" -> Boolean.TRUE.equals(container.getIsWeb());
            case "MOBILE" -> Boolean.TRUE.equals(container.getIsMobile());
            case "DESKTOP" -> Boolean.TRUE.equals(container.getIsDesktop());
            default -> true;
        };
    }
}

