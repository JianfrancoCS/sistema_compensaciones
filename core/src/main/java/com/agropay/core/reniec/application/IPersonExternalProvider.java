package com.agropay.core.reniec.application;

import com.agropay.core.reniec.models.PersonExternalInfo;

import java.util.Optional;

public interface IPersonExternalProvider {
    Optional<PersonExternalInfo> getPerson(String dni);
}
