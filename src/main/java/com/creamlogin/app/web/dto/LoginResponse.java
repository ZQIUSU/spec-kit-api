package com.creamlogin.app.web.dto;

public record LoginResponse(boolean ok, UserDto user) {

  public record UserDto(long id, String username) {}
}
