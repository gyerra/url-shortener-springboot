package com.urlshortener.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DisableUrlRequest {

    @NotNull(message = "disabled flag is required")
    private Boolean disabled;
}
