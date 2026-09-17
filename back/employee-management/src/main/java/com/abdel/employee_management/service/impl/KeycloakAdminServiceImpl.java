package com.abdel.employee_management.service.impl;

import com.abdel.employee_management.service.KeycloakAdminService;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class KeycloakAdminServiceImpl implements KeycloakAdminService {

    @Value("${keycloak.admin.server-url}")
    private String serverUrl;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    private Keycloak getAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType("client_credentials")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

    private RealmResource getRealmResource() {
        return getAdminClient().realm(realm);
    }

    @Override
    public String createUser(String name, String email, String password, String realmRole) {
        UsersResource usersResource = getRealmResource().users();

        String existingId = findUserIdByEmail(email);
        if (existingId != null) {
            updateUser(existingId, name, email, realmRole, password);
            return existingId;
        }

        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setRequiredActions(Collections.emptyList());

        String[] parts = (name != null ? name.trim() : "").split("\\s+", 2);
        String first = parts.length > 0 && !parts[0].isBlank() ? parts[0] : email;
        String last = parts.length > 1 && !parts[1].isBlank() ? parts[1] : (first.length() > 0 ? first : "User");
        user.setFirstName(first);
        user.setLastName(last);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password != null && !password.isBlank() ? password : "Password123!");
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));

        try (Response response = usersResource.create(user)) {
            if (response.getStatus() == 201) {
                String locationHeader = response.getHeaderString("Location");
                String keycloakId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

                assignRole(keycloakId, realmRole);
                return keycloakId;
            } else if (response.getStatus() == 409) {
                return findUserIdByEmail(email);
            } else {
                throw new RuntimeException("Failed to create user in Keycloak: HTTP " + response.getStatus());
            }
        }
    }

    @Override
    public void deleteUser(String keycloakId) {
        if (keycloakId == null) return;
        try {
            getRealmResource().users().get(keycloakId).remove();
        } catch (Exception err) {
            log.warn("Could not delete user {} from Keycloak: {}", keycloakId, err.getMessage());
        }
    }

    @Override
    public void updateUser(String keycloakId, String name, String email, String realmRole, String newPassword) {
        if (keycloakId == null) {
            keycloakId = findUserIdByEmail(email);
            if (keycloakId == null) {
                createUser(name, email, newPassword, realmRole);
                return;
            }
        }

        try {
            UserResource userResource = getRealmResource().users().get(keycloakId);
            UserRepresentation user = userResource.toRepresentation();

            user.setEmail(email);
            user.setUsername(email);
            user.setEnabled(true);
            user.setEmailVerified(true);
            user.setRequiredActions(Collections.emptyList());

            String[] parts = (name != null ? name.trim() : "").split("\\s+", 2);
            String first = parts.length > 0 && !parts[0].isBlank() ? parts[0] : (user.getFirstName() != null ? user.getFirstName() : email);
            String last = parts.length > 1 && !parts[1].isBlank() ? parts[1] : (first.length() > 0 ? first : "User");
            user.setFirstName(first);
            user.setLastName(last);

            userResource.update(user);

            if (newPassword != null && !newPassword.isBlank()) {
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(newPassword);
                credential.setTemporary(false);
                userResource.resetPassword(credential);
            }

            if (realmRole != null && !realmRole.isBlank()) {
                assignRole(keycloakId, realmRole);
            }
        } catch (NotFoundException e) {
            createUser(name, email, newPassword, realmRole);
        } catch (Exception err) {
            log.warn("Could not update user {} in Keycloak: {}", keycloakId, err.getMessage());
        }
    }

    private void assignRole(String keycloakId, String realmRole) {
        if (keycloakId == null || realmRole == null || realmRole.isBlank()) return;
        try {
            List<RoleRepresentation> currentRoles = getRealmResource().users().get(keycloakId).roles().realmLevel().listAll();
            if (currentRoles != null) {
                List<RoleRepresentation> toRemove = currentRoles.stream()
                        .filter(r -> ("SUPER_ADMIN".equals(r.getName()) || "NORMAL_USER".equals(r.getName())) && !r.getName().equals(realmRole))
                        .toList();
                if (!toRemove.isEmpty()) {
                    getRealmResource().users().get(keycloakId).roles().realmLevel().remove(toRemove);
                }
            }

            RoleRepresentation role = getRealmResource().roles().get(realmRole).toRepresentation();
            getRealmResource().users().get(keycloakId).roles().realmLevel().add(List.of(role));
        } catch (Exception e) {
            log.warn("Could not assign role {} to user {} in Keycloak: {}", realmRole, keycloakId, e.getMessage());
        }
    }

    @Override
    public String findUserIdByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        try {
            List<UserRepresentation> users = getRealmResource().users()
                    .searchByEmail(email, true);

            if (users != null && !users.isEmpty()) {
                return users.get(0).getId();
            }
        } catch (Exception e) {
            log.warn("Failed to search user by email {} in Keycloak: {}", email, e.getMessage());
        }
        return null;
    }
}
