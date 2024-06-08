package com.github.javakira.admin;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Admin {
    /**Id of telegram user*/
    @Id
    private long id;
}
