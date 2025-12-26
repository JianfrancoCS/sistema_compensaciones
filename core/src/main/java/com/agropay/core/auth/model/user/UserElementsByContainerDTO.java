package com.agropay.core.auth.model.user;

import java.util.List;
import java.util.UUID;

public record UserElementsByContainerDTO(
        UUID userPublicId,
        String username,
        List<ContainerWithElements> containers
) {
    public record ContainerWithElements(
            UUID containerPublicId,
            String containerName,
            String containerDisplayName,
            String containerIcon,
            List<ElementInfo> elements,
            List<UUID> selectedElementPublicIds
    ) {
    }
    
    public record ElementInfo(
            UUID publicId,
            String name,
            String displayName,
            String route,
            String icon
    ) {}
}

