package com.zwj.backend.entity;

import com.mybatisflex.annotation.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table("Tag")
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Tag {
    @Id
    private Long id;
    private String name;
}
