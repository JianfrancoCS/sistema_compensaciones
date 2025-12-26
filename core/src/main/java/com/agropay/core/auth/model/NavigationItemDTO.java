package com.agropay.core.auth.model;

import java.util.List;
import java.util.UUID;

public record NavigationItemDTO(
        String id,
        String displayName,
        String icon,
        String iconUrl,
        String route,
        List<NavigationItemDTO> children
) {
    public NavigationItemDTO(String id, String displayName, String icon, String route) {
        this(id, displayName, icon, null, route, List.of());
    }

    public NavigationItemDTO(String id, String displayName, String icon, String iconUrl, String route) {
        this(id, displayName, icon, iconUrl, route, List.of());
    }
}

