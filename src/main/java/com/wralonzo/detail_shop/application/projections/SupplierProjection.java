package com.wralonzo.detail_shop.application.projections;

public interface SupplierProjection {
  Long getId();

  String getName();

  String getEmail();

  String getPhone();

  String getAddress();

  String getCompanyName();
}