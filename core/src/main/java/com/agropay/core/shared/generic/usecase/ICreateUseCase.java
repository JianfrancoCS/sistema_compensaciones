package com.agropay.core.shared.generic.usecase;

public interface ICreateUseCase<M1,M2>{
    public M2 create(M1 model);
}
