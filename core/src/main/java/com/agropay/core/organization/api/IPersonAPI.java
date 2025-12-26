package com.agropay.core.organization.api;

import com.agropay.core.organization.domain.PersonEntity;

import java.time.LocalDate;

public interface IPersonAPI {
    PersonEntity findOrCreatePersonByDni(String documentNumber);
    void validateValueOfPerson(
            String names,
            String paternalLastname,
            String maternalLastname,
            String documentNumber,
            LocalDate dob,
            PersonEntity person
    );
}
