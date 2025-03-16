package com.birkann.dto.response;

import com.birkann.dto.DtoBase;
import com.birkann.enums.PlanType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO extends DtoBase {
    private String name;
    private String email;
    private Integer userCredit;
    private PlanType planType;
} 