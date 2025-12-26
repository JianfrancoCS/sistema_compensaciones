package com.agropay.core.states.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StateSelectOptionDTO {
    private UUID publicId;
    private String name;
    private boolean isDefault;
}
