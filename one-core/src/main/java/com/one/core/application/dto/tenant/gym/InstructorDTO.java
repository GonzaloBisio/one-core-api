package com.one.core.application.dto.tenant.gym;

import lombok.Data;

@Data
public class InstructorDTO {
    public Long id;
    public String name;
    public String email;
    public String phone;
    public String bio;
    public Boolean active = true;
}
