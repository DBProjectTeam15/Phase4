package knu.database.musicbase.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserDto {
    private String username;
    private long userId;
}
