package com.creamlogin.app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank
        @Size(min = 3, max = 32)
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "username may contain letters, digits, underscore only")
        String username,
    @NotBlank @Size(min = 8, max = 128) String password,
    @NotBlank @Size(max = 64) String realName,
    @NotBlank String idCardNumber) {}
