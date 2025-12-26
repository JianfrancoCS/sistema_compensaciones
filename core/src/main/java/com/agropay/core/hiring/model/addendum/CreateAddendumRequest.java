package com.agropay.core.hiring.model.addendum;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateAddendumRequest(
    // Contract reference
    @NotNull UUID contractPublicId,
    
    // Addendum details
    @NotNull @FutureOrPresent LocalDate startDate,
    LocalDate endDate, // Optional
    @NotNull UUID templatePublicId,
    @NotNull UUID addendumTypePublicId,

    // Non-default variables
    @Valid List<AddendumVariableValuePayload> variables
) {}