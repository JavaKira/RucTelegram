package com.github.javakira.context;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserContextRepository extends JpaRepository<UserContext, Long> {
}
