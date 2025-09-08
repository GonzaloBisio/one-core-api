package com.one.core.application.dto.tenant.gym;

import lombok.Data;

import java.util.List;

@Data
public class ClassTypeDTO {
    public Long id;
    public String name;
    public String description;
    public List<String> tags;
    public Boolean active = true;
}
