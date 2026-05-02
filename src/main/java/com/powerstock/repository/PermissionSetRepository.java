package com.powerstock.repository;
import com.powerstock.model.entity.PermissionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PermissionSetRepository extends JpaRepository<PermissionSet, Long> {
    List<PermissionSet> findAll();
}
