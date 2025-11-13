package com.forristall.general.idp.entity;

import java.util.Objects;
import java.util.Set;

import com.forristall.general.idp.entity.Role.Application;
import com.google.gson.Gson;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "AdditionalPermissions")
public class AdditionalPermission {

  private @Id @GeneratedValue @NotNull Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "application")
  private Application application;

  private String permission;

  @ManyToMany
  private Set<User> users;

  public AdditionalPermission() {
    super();
  }

  public AdditionalPermission(
          @NotNull Long id,
          Application application,
          String permission,
          Set<User> users) {
    super();
    this.id = id;
    this.application = application;
    this.permission = permission;
    this.users = users;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Application getApplication() {
    return application;
  }

  public void setApplication(Application application) {
    this.application = application;
  }

  public String getPermission() {
    return permission;
  }

  public void setPermission(String permission) {
    this.permission = permission;
  }

  public Set<User> getUsers() {
    return users;
  }

  public void setUsers(Set<User> users) {
    this.users = users;
  }

  @Override
  public int hashCode() {
    return Objects.hash(application, id, permission, users);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AdditionalPermission other = (AdditionalPermission) obj;
    return application == other.application && Objects.equals(id, other.id)
            && Objects.equals(permission, other.permission) && Objects.equals(users, other.users);
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

}
