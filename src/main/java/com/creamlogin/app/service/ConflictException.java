package com.creamlogin.app.service;

/** Thrown when a domain rule conflicts with uniqueness (HTTP 409). */
public class ConflictException extends RuntimeException {

  private final String code;

  public ConflictException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
