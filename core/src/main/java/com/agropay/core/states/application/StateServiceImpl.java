package com.agropay.core.states.application;

import com.agropay.core.states.domain.StateEntity;
import com.agropay.core.shared.exceptions.DataIntegrityViolationException;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.states.mapper.IStateMapper;
import com.agropay.core.states.models.StateSelectOptionDTO;
import com.agropay.core.states.persistence.StateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StateServiceImpl implements IStateUseCase {

    private final StateRepository stateRepository;
    private final IStateMapper stateMapper;


    @Override
    @Transactional(readOnly = true)
    public List<StateSelectOptionDTO> findStateOptionsByDomainName(String domainName) {
        List<StateEntity> entities = stateRepository.findByDomainName(domainName);
        this.checkNotDuplicateIsDefault(entities);
        return stateMapper.toSelectOptionDto(entities);
    }

    @Override
    public StateEntity findDefaultStateByDomainName(String domainName) {
        Optional<StateEntity> byIsDefaultTrueAndDomainName = stateRepository.findByIsDefaultTrueAndDomainName(domainName);
        if (byIsDefaultTrueAndDomainName.isEmpty()) {
            throw new IdentifierNotFoundException("exception.state.not-found-default", domainName);
        }
        return byIsDefaultTrueAndDomainName.get();
    }

    private void checkNotDuplicateIsDefault(List<StateEntity> states) {
        List<UUID> defaultStateIds = new ArrayList<>();
        int defaultCount = 0;

        for (StateEntity state : states) {
            if (state.isDefault()) {
                defaultCount++;
                defaultStateIds.add(state.getPublicId());
            }
        }
        if (defaultCount > 1) {
            log.error("Se encontraron {} estados con isDefault=true: {}",
                    defaultCount, defaultStateIds);

            throw new DataIntegrityViolationException("exception.shared.data-violation-integrity",this.getClass().getSimpleName(),defaultCount);
        }
    }
    @Override
    public StateEntity findByPublicId(UUID publicId) {
        Optional<StateEntity> byPublicId = stateRepository.findByPublicId(publicId);
        if (byPublicId.isEmpty()) {
            throw new IdentifierNotFoundException("exception.state.not-found", publicId);
        }
        return byPublicId.get();
    }

    @Override
    public StateEntity findByCodeAndDomainName(String code, String domain) {
        Optional<StateEntity> byCode = stateRepository.findByCodeAndDomainName(code, domain);
        if (byCode.isEmpty()) {
            throw new IdentifierNotFoundException("exception.state.not-found-by-code", code);
        }
        return byCode.get();
    }
}
