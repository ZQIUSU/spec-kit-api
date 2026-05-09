package com.creamlogin.app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank @Size(min = 1, max = 64) String username, @NotBlank String password) {}
