package com.wralonzo.detail_shop.application.projections;

public interface PositionTypeProjection {
    Long getId();

    String getName();

    int getLevel();

    // Spring ejecutará un COUNT en lugar de cargar todos los objetos
    //@Value("#{target.employees.size()}")
    //Integer getEmployeeCount();

}
