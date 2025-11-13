package com.forristall.general.idp.util;

import java.sql.Timestamp;
import java.util.Objects;

import com.forristall.general.idp.util.RestError;
import com.google.gson.Gson;

public class HttpErrorResponse {

  private Timestamp timestamp;

  private int status;

  private String error;

  private String message;

  private String path;

  public HttpErrorResponse() {
    super();
  }

  public HttpErrorResponse(
          Timestamp timestamp,
          int status,
          String error,
          String message,
          String path) {
    super();
    this.timestamp = timestamp;
    this.status = status;
    this.error = error;
    this.message = message;
    this.path = path;
  }

  public Timestamp getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Timestamp timestamp) {
    this.timestamp = timestamp;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getMessage() {
    return message;
  }

  public RestError getMessageAsRestError() {
    return new Gson().fromJson(message, RestError.class);
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public int hashCode() {
    return Objects.hash(error, message, path, status, timestamp);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    HttpErrorResponse other = (HttpErrorResponse) obj;
    return Objects.equals(error, other.error) && Objects.equals(message, other.message)
            && Objects.equals(path, other.path) && status == other.status
            && Objects.equals(timestamp, other.timestamp);
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

}
