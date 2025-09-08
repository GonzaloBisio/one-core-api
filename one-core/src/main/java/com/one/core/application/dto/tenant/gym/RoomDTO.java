package com.one.core.application.dto.tenant.gym;

import lombok.Data;

@Data
public class RoomDTO {
    public Long id;
    public String name;
    public String location;
    public Integer capacityDefault;
    public Boolean active = true;
}
