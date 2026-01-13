package com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

}
