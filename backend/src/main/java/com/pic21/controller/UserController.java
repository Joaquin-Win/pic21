/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.controller.UserController
 *  com.pic21.dto.request.UpdateUserRequest
 *  com.pic21.dto.response.UserResponse
 *  com.pic21.exception.BusinessException
 *  com.pic21.service.UserService
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.security.core.annotation.AuthenticationPrincipal
 *  org.springframework.security.core.userdetails.UserDetails
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PatchMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package com.pic21.controller;

import com.pic21.dto.request.UpdateUserRequest;
import com.pic21.dto.response.UserResponse;
import com.pic21.exception.BusinessException;
import com.pic21.service.UserService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/users"})
@PreAuthorize(value="hasRole('R04_ADMIN')")
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok((Object)this.userService.findAll());
    }

    @GetMapping(value={"/{id}"})
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok((Object)this.userService.findById(id));
    }

    @PutMapping(value={"/{id}"})
    public ResponseEntity<UserResponse> updateProfile(@PathVariable Long id, @RequestBody UpdateUserRequest request, @AuthenticationPrincipal UserDetails me) {
        return ResponseEntity.ok((Object)this.userService.updateProfile(id, request, me.getUsername()));
    }

    @PutMapping(value={"/{id}/roles"})
    public ResponseEntity<UserResponse> updateRoles(@PathVariable Long id, @RequestBody Map<String, List<String>> body, @AuthenticationPrincipal UserDetails me) {
        List<String> roles = body.get("roles");
        if (roles == null || roles.isEmpty()) {
            throw new BusinessException("El campo 'roles' es requerido y no puede estar vac\u00edo.");
        }
        return ResponseEntity.ok((Object)this.userService.updateRoles(id, roles, me.getUsername()));
    }

    @PatchMapping(value={"/{id}/toggle"})
    public ResponseEntity<UserResponse> toggleActivo(@PathVariable Long id, @AuthenticationPrincipal UserDetails me) {
        return ResponseEntity.ok((Object)this.userService.toggleActivo(id, me.getUsername()));
    }

    @DeleteMapping(value={"/{id}"})
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails me) {
        this.userService.delete(id, me.getUsername());
        return ResponseEntity.noContent().build();
    }

    public UserController(UserService userService) {
        this.userService = userService;
    }
}

