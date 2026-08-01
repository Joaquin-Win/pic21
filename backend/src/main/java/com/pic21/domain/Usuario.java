/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.Credencial
 *  com.pic21.domain.PerfilEstudiantil
 *  com.pic21.domain.PerfilPersonal
 *  com.pic21.domain.Rol
 *  com.pic21.domain.Usuario
 *  com.pic21.domain.Usuario$UsuarioBuilder
 *  jakarta.persistence.CascadeType
 *  jakarta.persistence.CollectionTable
 *  jakarta.persistence.Column
 *  jakarta.persistence.ElementCollection
 *  jakarta.persistence.Embedded
 *  jakarta.persistence.Entity
 *  jakarta.persistence.EnumType
 *  jakarta.persistence.Enumerated
 *  jakarta.persistence.FetchType
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.JoinColumn
 *  jakarta.persistence.OneToOne
 *  jakarta.persistence.Table
 *  org.hibernate.annotations.CreationTimestamp
 */
package com.pic21.domain;
import lombok.Builder;

import com.pic21.domain.Credencial;
import com.pic21.domain.PerfilEstudiantil;
import com.pic21.domain.PerfilPersonal;
import com.pic21.domain.Rol;
import com.pic21.domain.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import org.hibernate.annotations.CreationTimestamp;

/*
 * Exception performing whole class analysis ignored.
 */
@Entity
@Table(name="usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=80)
    private String nombre;
    @Column(nullable=false, length=80)
    private String apellido;
    @Column(unique=true, nullable=false, length=50)
    private String username;
    @ElementCollection(fetch=FetchType.EAGER)
    @Enumerated(value=EnumType.STRING)
    @CollectionTable(name="usuario_roles", joinColumns={@JoinColumn(name="usuario_id")})
    @Column(name="rol", length=20)
    private Set<Rol> roles;
    @CreationTimestamp
    @Column(name="fecha_registro", updatable=false)
    private LocalDateTime fechaRegistro;
    @Column(nullable=false)
    private boolean activo;
    @OneToOne(cascade={CascadeType.ALL}, orphanRemoval=true, fetch=FetchType.EAGER)
    @JoinColumn(name="credencial_id", nullable=false, unique=true)
    private Credencial credencial;
    @Embedded
    private PerfilPersonal perfilPersonal;
    @Embedded
    private PerfilEstudiantil perfilEstudiantil;

    public boolean esGrupoA() {
        return this.roles.contains(Rol.R01_PROFESOR) || this.roles.contains(Rol.R03_EGRESADO)
            || this.roles.contains(Rol.R04_ADMIN) || this.roles.contains(Rol.R05_DIRECTOR)
            || this.roles.contains(Rol.R07_ESTUDIANTE_POSGRADO);
    }

    public boolean esGrupoB() {
        return this.roles.contains(Rol.R02_ESTUDIANTE) || this.roles.contains(Rol.R06_AYUDANTE);
    }

    private static Set<Rol> $default$roles() {
        return EnumSet.noneOf(Rol.class);
    }

    private static boolean $default$activo() {
        return true;
    }
    public Long getId() {
        return this.id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public String getApellido() {
        return this.apellido;
    }

    public String getUsername() {
        return this.username;
    }

    public Set<Rol> getRoles() {
        return this.roles;
    }

    public LocalDateTime getFechaRegistro() {
        return this.fechaRegistro;
    }

    public boolean isActivo() {
        return this.activo;
    }

    public Credencial getCredencial() {
        return this.credencial;
    }

    public PerfilPersonal getPerfilPersonal() {
        return this.perfilPersonal;
    }

    public PerfilEstudiantil getPerfilEstudiantil() {
        return this.perfilEstudiantil;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRoles(Set<Rol> roles) {
        this.roles = roles;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public void setCredencial(Credencial credencial) {
        this.credencial = credencial;
    }

    public void setPerfilPersonal(PerfilPersonal perfilPersonal) {
        this.perfilPersonal = perfilPersonal;
    }

    public void setPerfilEstudiantil(PerfilEstudiantil perfilEstudiantil) {
        this.perfilEstudiantil = perfilEstudiantil;
    }

    public Usuario() {
        this.roles = Usuario.$default$roles();
        this.activo = Usuario.$default$activo();
    }

    @Builder
    public Usuario(Long id, String nombre, String apellido, String username, Set<Rol> roles, LocalDateTime fechaRegistro, boolean activo, Credencial credencial, PerfilPersonal perfilPersonal, PerfilEstudiantil perfilEstudiantil) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.username = username;
        this.roles = roles;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
        this.credencial = credencial;
        this.perfilPersonal = perfilPersonal;
        this.perfilEstudiantil = perfilEstudiantil;
    }
}

