package com.agropay.core.organization.persistence;

import com.agropay.core.organization.domain.CompanyEntity;
import com.agropay.core.shared.generic.persistence.IFindByPublicIdRepository;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICompanyRepository extends ISoftRepository<CompanyEntity, Long>, IFindByPublicIdRepository<CompanyEntity> {

    @Query("SELECT c FROM CompanyEntity AS c WHERE c.ruc = :ruc")
    Optional<CompanyEntity> findByRuc(String ruc);

    @Query(value = "SELECT TOP 1 * FROM app.tbl_companies ORDER BY id", nativeQuery = true)
    Optional<CompanyEntity> getPrimaryCompany();

}
