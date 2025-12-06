package knu.database.musicbase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SongDetailDto {
    private Long id;
    private String title;
    private String playLink;
    private Integer length;     // 초 단위
    private String createAt;    // ISO 8601 String
    private String providerName;
    private String artistName;

}