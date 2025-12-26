package com.agropay.core.hiring.api;

import com.agropay.core.hiring.domain.ContractEntity;

import java.util.UUID;

public interface IContractAPI {

   ContractEntity findByPublicId(UUID publicId);
}
