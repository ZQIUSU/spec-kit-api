package com.creamlogin.app.web.dto;

public record LoginResponse(boolean ok, String token, UserDto user) {

  public record UserDto(long id, String username, String role, int points) {}
}
