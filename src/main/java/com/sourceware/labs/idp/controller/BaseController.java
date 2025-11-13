package com.sourceware.labs.idp.controller;

public abstract class BaseController {
  
  protected String BASE_PATH;
  
  protected String getRoutePath(String path) {
    return BASE_PATH + "/" + path.split("/")[1];
  }
}
