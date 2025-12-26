package com.agropay.core.shared.generic.usecase;

import java.util.List;

public interface IGetAllUseCase<M> {
    List<M> getAll();
}
