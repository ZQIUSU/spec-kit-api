package com.creamlogin.app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecoverPasswordRequest(
    @NotBlank @Size(max = 64) String username,
    @NotBlank @Size(max = 64) String realName,
    @NotBlank String idCardNumber) {}
