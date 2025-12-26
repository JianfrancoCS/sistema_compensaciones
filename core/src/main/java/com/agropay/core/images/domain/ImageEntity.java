
package com.agropay.core.images.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tbl_images", schema = "app")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageEntity extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(name = "imageable_id", nullable = false, length = 255)
    private String imageableId;

    @Column(name = "imageable_type", nullable = false)
    private String imageableType;

    @Column(nullable = false)
    private String url;

    @Column(name = "order_value", nullable = false)
    private Integer orderValue;

}
