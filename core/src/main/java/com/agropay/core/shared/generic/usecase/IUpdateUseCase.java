package com.agropay.core.shared.generic.usecase;

public interface IUpdateUseCase<M1,M2, ID> {
    M2 update(M1 model, ID id);
}
