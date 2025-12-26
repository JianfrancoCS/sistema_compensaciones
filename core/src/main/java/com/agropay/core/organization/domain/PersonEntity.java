package com.agropay.core.organization.domain;

import com.agropay.core.address.application.IAddressableUseCase;
import com.agropay.core.images.application.usecase.IImageable;
import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Table(name = "tbl_persons", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonEntity extends AbstractEntity<String> implements IAddressableUseCase<String>, IImageable {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "document_number", length = 15) // Flexible length for different document types
    private String documentNumber;

    @Column(name = "names", nullable = false)
    private String names;

    @Column(name = "paternal_lastname", nullable = false)
    private String paternalLastname;

    @Column(name = "maternal_lastname", nullable = false)
    private String maternalLastname;

    private LocalDate dob;

    @Column(name = "gender", length = 1)
    private String gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", referencedColumnName = "id")
    private DistrictEntity district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id", referencedColumnName = "id")
    private DocumentTypeEntity documentType;

    @Column(name = "person_parent_document_number", length = 15)
    private String personParentDocumentNumber;

    @Override
    public String getId() {
        return this.documentNumber;
    }

    @Override
    public String getAddressableType() {
        return this.getClass().getSimpleName();
    }

    // Implementación de IImageable - getId() ya existe y retorna documentNumber
    @Override
    public String getSimpleName() {
        return this.getClass().getSimpleName();
    }
}
