package knu.database.musicbase.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManagerLoginDto {
    private String id;
    private String password;
}
