package com.forristall.general.idp.entity;

import java.sql.Timestamp;
import java.util.Objects;

import com.forristall.general.idp.entity.Role.Application;
import com.google.gson.Gson;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "AuditLog")
public class AuditLog {

  public enum Event {
    Login, Signup, Recover, Day, Task, Timeblock
  }

  public enum Action {
    Create, Read, Update, Delete, Share
  }

  public enum Status {
    Success, Failure
  }

  private @Id @GeneratedValue @NotNull Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "application")
  private Application application;

  @Enumerated(EnumType.STRING)
  @Column(name = "event")
  private Event event;

  @Enumerated(EnumType.STRING)
  @Column(name = "action")
  private Action action;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private Status status;

  private Timestamp created;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  public AuditLog() {
    super();
  }

  public AuditLog(
          @NotNull Long id,
          Application application,
          Event event,
          Action action,
          Status status,
          Timestamp created,
          User user) {
    super();
    this.id = id;
    this.application = application;
    this.event = event;
    this.action = action;
    this.status = status;
    this.created = created;
    this.user = user;
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

  public Event getEvent() {
    return event;
  }

  public void setEvent(Event event) {
    this.event = event;
  }

  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Timestamp getCreated() {
    return created;
  }

  public void setCreated(Timestamp created) {
    this.created = created;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public int hashCode() {
    return Objects.hash(action, application, created, event, id, status, user);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AuditLog other = (AuditLog) obj;
    return action == other.action && application == other.application
            && Objects.equals(created, other.created) && event == other.event
            && Objects.equals(id, other.id) && status == other.status
            && Objects.equals(user, other.user);
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

}
