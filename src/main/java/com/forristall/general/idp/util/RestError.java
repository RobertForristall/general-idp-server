package com.forristall.general.idp.util;

import java.util.Objects;

import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

public class RestError {

  private String route;

  private RequestMethod method;

  private Integer errorCode;

  private String msg;

  public RestError() {
    super();
  }

  public RestError(String route, RequestMethod method, int errorCode, String msg) {
    super();
    this.route = route;
    this.method = method;
    this.errorCode = errorCode;
    this.msg = msg;
  }

  public String getRoute() {
    return route;
  }

  public RequestMethod getMethod() {
    return method;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public String getMsg() {
    return msg;
  }

  @Override
  public int hashCode() {
    return Objects.hash(errorCode, method, msg, route);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RestError other = (RestError) obj;
    return errorCode == other.errorCode && method == other.method && Objects.equals(msg, other.msg)
            && Objects.equals(route, other.route);
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

  public static class RestErrorBuilder {
    private String route;

    private RequestMethod method;

    private Integer errorCode;

    private String msg;

    public RestErrorBuilder() {
      super();
    }

    public RestErrorBuilder setRoute(String route) {
      this.route = route;
      return this;
    }

    public RestErrorBuilder setMethod(RequestMethod method) {
      this.method = method;
      return this;
    }

    public RestErrorBuilder setErrorCode(Integer errorCode) {
      this.errorCode = errorCode;
      return this;
    }

    public RestErrorBuilder setMsg(String msg) {
      this.msg = msg;
      return this;
    }

    public boolean isErrorCodeSet() {
      return errorCode != null;
    }

    public RestError build() {
      return new RestError(route, method, errorCode, msg);
    }
  }
}
