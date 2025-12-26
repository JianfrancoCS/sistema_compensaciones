package com.agropay.core.shared.generic.usecase;

import com.agropay.core.shared.exceptions.InvalidSortFieldException;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IGetAllPaged<M>{
    PagedResult<M> findAllPaged(String name, Pageable pageable);

}
