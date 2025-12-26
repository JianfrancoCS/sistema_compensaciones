package com.agropay.core.payroll.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the possible states of a payroll entity.
 * These values must match the codes in tbl_states for domain 'tbl_payrolls'.
 */
@Getter
@RequiredArgsConstructor
public enum PayrollState {

    /**
     * Initial state when payroll is created.
     * Payroll can be edited and deleted in this state.
     */
    DRAFT("PAYROLL_DRAFT", "Draft"),

    /**
     * Payroll calculation batch job is currently running.
     * Payroll cannot be edited or deleted in this state.
     */
    IN_PROGRESS("PAYROLL_IN_PROGRESS", "In Progress"),

    /**
     * Payroll calculation has completed successfully.
     * Payroll is ready for review and approval.
     */
    CALCULATED("PAYROLL_CALCULATED", "Calculated"),

    /**
     * Payroll has been reviewed and approved.
     * Ready for payment processing.
     */
    APPROVED("PAYROLL_APPROVED", "Approved"),

    /**
     * Payroll has been paid to employees.
     * This is a final state, cannot be modified.
     */
    PAID("PAYROLL_PAID", "Paid"),

    /**
     * Payroll has been cancelled.
     * This is a final state.
     */
    CANCELLED("PAYROLL_CANCELLED", "Cancelled"),

    /**
     * Payroll has been cancelled to allow corrections.
     * A corrected payroll should be created referencing this one.
     */
    CANCELLED_CORRECTION("PAYROLL_CANCELLED_CORRECTION", "Cancelled for Correction");

    /**
     * State code as stored in tbl_states.code
     */
    private final String code;

    /**
     * Human-readable display name
     */
    private final String displayName;

    /**
     * Get enum by code
     */
    public static PayrollState fromCode(String code) {
        for (PayrollState state : values()) {
            if (state.code.equals(code)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown payroll state code: " + code);
    }

    /**
     * Check if payroll can be deleted in this state.
     * Only DRAFT payrolls can be soft-deleted.
     */
    public boolean isDeletable() {
        return this == DRAFT;
    }

    /**
     * Check if payroll can be edited in this state.
     * Only DRAFT payrolls can be modified.
     */
    public boolean isEditable() {
        return this == DRAFT;
    }

    /**
     * Check if payroll calculation can be launched from this state.
     * Only DRAFT payrolls can have calculation launched.
     */
    public boolean canLaunchCalculation() {
        return this == DRAFT;
    }
}