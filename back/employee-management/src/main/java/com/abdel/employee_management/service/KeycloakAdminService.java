package com.abdel.employee_management.service;

/**
 * Manages user provisioning in Keycloak via the Admin REST API.
 */
public interface KeycloakAdminService {

    /**
     * Creates a user in Keycloak with the given credentials and role.
     * Returns the Keycloak user ID (the "sub" claim).
     */
    String createUser(String name, String email, String password, String realmRole);

    /**
     * Deletes a user from Keycloak by their Keycloak ID.
     */
    void deleteUser(String keycloakId);

    /**
     * Updates a user's name, email, and optionally role and password in Keycloak.
     */
    void updateUser(String keycloakId, String name, String email, String realmRole, String newPassword);

    /**
     * Looks up a Keycloak user by email and returns their ID, or null if not found.
     */
    String findUserIdByEmail(String email);
}
