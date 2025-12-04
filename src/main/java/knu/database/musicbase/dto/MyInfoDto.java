package knu.database.musicbase.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyInfoDto {
    private long id;
    private String username;
}
