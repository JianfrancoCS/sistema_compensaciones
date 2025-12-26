package com.agropay.core.states.application;

import com.agropay.core.states.domain.StateEntity;
import com.agropay.core.states.models.StateSelectOptionDTO;

import java.util.List;
import java.util.UUID;

public interface IStateUseCase {
    List<StateSelectOptionDTO> findStateOptionsByDomainName(String domainName);
    StateEntity findDefaultStateByDomainName(String domainName);
    StateEntity findByPublicId(UUID publicId);
    StateEntity findByCodeAndDomainName(String code,String domainName);

}
