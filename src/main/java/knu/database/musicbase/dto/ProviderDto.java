package knu.database.musicbase.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProviderDto {
    private long id;
    private String name;
    private String link;
}
