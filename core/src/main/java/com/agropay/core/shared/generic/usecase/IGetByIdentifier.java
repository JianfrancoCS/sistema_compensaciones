package com.agropay.core.shared.generic.usecase;

public interface IGetByIdentifier<M,ID> {
    M getByIdentifier(ID identifier);
}
