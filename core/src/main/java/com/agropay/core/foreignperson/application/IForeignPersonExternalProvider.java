package com.agropay.core.foreignperson.application;

import com.agropay.core.foreignperson.models.ForeignPersonExternalInfo;

import java.util.Optional;

public interface IForeignPersonExternalProvider {
    Optional<ForeignPersonExternalInfo> getForeignPerson(String documentNumber, String birthdate);
}

