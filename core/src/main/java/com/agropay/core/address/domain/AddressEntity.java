package com.agropay.core.address.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.util.Objects;
import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_addresses", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressEntity extends AbstractEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false)
    private UUID publicId;

    @Column(name = "addressable_type", nullable = false)
    private String addressableType;

    @Column(name = "addressable_id", nullable = false)
    private String addressableId;

    private String longitude;
    private String latitude;

    @Column(name = "address_text", length = 500)
    private String addressText;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary = false;

    public boolean exactLocation(String longitude, String latitude){
      boolean result = false;
      if(Objects.isNull(longitude) && Objects.isNull(latitude)){
          return result;
      }
      if(this.longitude.equals(longitude)  && this.latitude.equals(latitude)){
          result = true;
      }
      return result;

    };

}

