package com.wralonzo.detail_shop.modules.auth.domain.jpa.specs;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Employee;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Profile;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Role;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.entities.Client;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public class UserSpecifications {

  public static Specification<User> filterUsers(String term, String roleName) {
    return (root, query, cb) -> {
      // 1. Evitar duplicados si hay Joins con colecciones
      query.distinct(true);

      List<Predicate> predicates = new ArrayList<>();

      // 3. Filtro: roleName (Join con roles)
      if (roleName != null && !roleName.isEmpty()) {
        // Especificamos que se una a nuestra entidad Role
        Join<User, Role> rolesJoin = root.join("roles");
        predicates.add(cb.equal(rolesJoin.get("name"), roleName));
      }

      // 4. Filtro: term (username o fullName)
      if (term != null && !term.trim().isEmpty()) {
        String pattern = "%" + term.toLowerCase() + "%";

        // Hacemos el JOIN programático con Profile
        Join<User, Profile> profileJoin = root.join("profile", jakarta.persistence.criteria.JoinType.LEFT);

        Predicate usernameLike = cb.like(cb.lower(root.get("username")), pattern);
        Predicate fullNameLike = cb.like(cb.lower(profileJoin.get("fullName")), pattern);

        predicates.add(cb.or(usernameLike, fullNameLike));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  public static Specification<User> searchByTerm(String term) {
    return (root, query, cb) -> {
      if (term == null || term.isBlank())
        return cb.conjunction();

      String pattern = "%" + term.toLowerCase() + "%";

      // Hacemos el JOIN programático con Profile
      Join<User, Profile> profileJoin = root.join("profile");

      // WHERE (LOWER(username) LIKE %term% OR LOWER(profile.fullName) LIKE %term%)
      return cb.or(
          cb.like(cb.lower(root.get("username")), pattern),
          cb.like(cb.lower(profileJoin.get("fullName")), pattern));
    };
  }

  public static Specification<User> hasUserType(String type) {
    return (root, query, cb) -> {
      if (type == null || type.isBlank())
        return null;

      if (type.equalsIgnoreCase("CLIENT")) {
        // 1. Definir la subconsulta
        Subquery<Long> clientSubquery = query.subquery(Long.class);
        Root<Client> clientRoot = clientSubquery.from(Client.class);

        // 2. Construir predicados para el Cliente
        Predicate isSameUser = cb.equal(clientRoot.get("userId"), root.get("id"));
        Predicate clientNotDeleted = cb.isNull(clientRoot.get("deletedAt")); // <-- Filtro solicitado
        Predicate clientActive = cb.isTrue(clientRoot.get("active")); // Opcional: solo si están activos

        // 3. Configurar subconsulta: SELECT user_id FROM customer.clients WHERE ...
        clientSubquery.select(clientRoot.get("userId"))
            .where(cb.and(isSameUser, clientNotDeleted, clientActive));

        // 4. El usuario no debe ser empleado Y debe existir en la subconsulta de
        // clientes válidos
        return cb.and(
            cb.isNull(root.get("employee")),
            cb.exists(clientSubquery));
      }

      if (type.equalsIgnoreCase("EMPLOYEE")) {
        return cb.isNotNull(root.get("employee"));
      }

      return null;
    };
  }

  public static Specification<User> filterByCompanyContext(Long companyId, List<Long> warehouseIdsOfCompany) {
    return (root, query, cb) -> {
      query.distinct(true);

      // --- LÓGICA PARA EMPLEADOS ---
      // Unimos con Employee (LEFT JOIN para no perder a los clientes en la consulta)
      Join<User, Employee> empJoin = root.join("employee", jakarta.persistence.criteria.JoinType.LEFT);

      // El empleado debe estar en uno de los warehouses de la empresa del Admin
      Predicate isEmployeeOfMyCompany = cb.and(
          cb.isNotNull(root.get("employee")),
          empJoin.get("warehouseId").in(warehouseIdsOfCompany));

      // --- LÓGICA PARA CLIENTES ---
      // Subconsulta para verificar que el cliente pertenezca a la compañía en el
      // esquema customer
      Subquery<Long> clientSubquery = query.subquery(Long.class);
      Root<Client> clientRoot = clientSubquery.from(Client.class);

      clientSubquery.select(clientRoot.get("userId"))
          .where(cb.and(
              cb.equal(clientRoot.get("userId"), root.get("id")), // Join por ID de usuario
              cb.equal(clientRoot.get("companyId"), companyId), // Filtro por compañía
              cb.isNull(clientRoot.get("deletedAt")) // Solo no eliminados
      ));

      // Un usuario es "mi cliente" si no tiene cargo de empleado y existe en la
      // subquery con mi companyId
      Predicate isClientOfMyCompany = cb.and(
          cb.isNull(root.get("employee")),
          cb.exists(clientSubquery));

      // El resultado final es la unión de ambos casos
      return cb.or(isEmployeeOfMyCompany, isClientOfMyCompany);
    };
  }

  public static Specification<User> isNotDeleted() {
    return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
  }
}