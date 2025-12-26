package com.agropay.core.shared.generic.usecase;

import java.util.List;

public interface IGetAllByIdentifier <M,I>{
    List<M> getAllByIdentifier(I identifier);
}
