package com.employeemgmt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayrollRequest {

    @Min(1)
    @Max(12)
    @JsonProperty("month")
    private int payMonth;

    @NotNull
    @JsonProperty("year")
    private int payYear;
}
