package knu.database.musicbase.controller.search;

import knu.database.musicbase.console.PageController;
import knu.database.musicbase.console.PageKey;
import knu.database.musicbase.service.SongService;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;

@RequiredArgsConstructor
public class SongSearchController implements PageController<PageKey> {

    private final SongService songService;

    @Override
    public void displayScreen() {
        System.out.println("--- Song Search ---");
        System.out.println("--- [현재 검색 필터] ---");

        String f1 = "1. 곡명: " + (songService.getTitleKeyword() != null ? songService.getTitleKeyword() + (songService.isTitleExact() ? " (완전일치)" : " (포함)") : "(설정 안함)");
        String f2 = "2. 아티스트: " + (songService.getArtistKeyword() != null ? songService.getArtistKeyword() + (songService.isArtistExact() ? " (완전일치)" : " (포함)") : "(설정 안함)");
        String f3 = "3. 재생시간: " + (songService.getLengthMin() != null ? songService.getLengthMin() + "초" : "N/A") + " ~ " + (songService.getLengthMax() != null ? songService.getLengthMax() + "초" : "N/A");
        String f4 = "4. 제공원: " + (songService.getProviderKeyword() != null ? songService.getProviderKeyword() + (songService.isProviderExact() ? " (완전일치)" : " (포함)") : "(설정 안함)");
        String f5 = "5. 발매일: " + (songService.getDateMin() != null ? songService.getDateMin() : "N/A") + " ~ " + (songService.getDateMax() != null ? songService.getDateMax() : "N/A");

        // Service에 저장된 SQL 컬럼명(orderBy)을 다시 사용자 친화적 이름으로 변환
        String orderBySimple = "곡명";
        for (Map.Entry<String, String> entry : SongService.getColumnMap().entrySet()) {
            if (entry.getValue().equals(songService.getOrderBy())) {
                orderBySimple = entry.getKey();
                break;
            }
        }
        String f6 = "6. 정렬: " + (orderBySimple) + " " + (songService.getOrderDir());

        System.out.printf("%-45s %-45s %-45s\n", f1, f2, f3);
        System.out.printf("%-45s %-45s %-45s\n", f4, f5, f6);
        System.out.println("1. 곡명 필터 (사용법: 1 [키워드] [완전일치|NULL] | 1)");
        System.out.println("2. 아티스트 필터 (사용법: 2 [키워드] [완전일치|NULL] | 2 NULL)");
        System.out.println("3. 재생시간 필터 (초/분:초) (사용법: 3 [min] [max] | 3 NULL 180)");
        System.out.println("4. 제공원 필터 (사용법: 4 [키워드] [완전일치|NULL] | 4 NULL)");
        System.out.println("5. 발매일 필터 (YYYY-MM-DD) (사용법: 5 [min] [max] | 5 NULL 2023-01-01)");
        System.out.println("6. 정렬 기준 (사용법: 6 [곡명|아티스트|...] [오름차순|내림차순])");
        System.out.println("7. 검색");
        System.out.println("0. 돌아가기 (검색 메뉴)");
    }

    @Override
    public PageKey invoke(String[] commands) {
        return switch (commands[0]) {
            case "1" -> {
                // --- 1. 곡명으로 검색 및 실행 ---
                if (commands.length == 1) {
                    songService.setTitleKeyword(null);
                    songService.setTitleExact(false);
                } else {
                    String[] keywordParts = parseKeywordAndExact(commands);
                    songService.setTitleKeyword(keywordParts[0]);
                    songService.setTitleExact(Boolean.parseBoolean(keywordParts[1]));
                }


                yield PageKey.SONG_SEARCH; // 현재 페이지 유지
            }
            case "2" -> {
                // --- 2. 아티스트 필터 ---
                if (commands.length < 2 || commands[1].equalsIgnoreCase("NULL")) {
                    songService.setArtistKeyword(null);
                } else {
                    String[] parts = parseKeywordAndExact(commands);
                    songService.setArtistKeyword(parts[0]);
                    songService.setArtistExact(Boolean.parseBoolean(parts[1]));
                }
                yield PageKey.SONG_SEARCH;
            }
            case "3" -> {
                // --- 3. 재생시간 필터 ---
                if (commands.length < 3) {
                    System.out.println("오류: 최소(min)와 최대(max) 값을 모두 입력해야 합니다.");
                } else {
                    songService.setLengthMin(parseLengthToSeconds(commands[1]));
                    songService.setLengthMax(parseLengthToSeconds(commands[2]));
                }
                yield PageKey.SONG_SEARCH;
            }
            case "4. 제공원 필터" -> {
                if (commands.length < 2 || commands[1].equalsIgnoreCase("NULL")) {
                    songService.setProviderKeyword(null);
                } else {
                    String[] parts = parseKeywordAndExact(commands);
                    songService.setProviderKeyword(parts[0]);
                    songService.setProviderExact(Boolean.parseBoolean(parts[1]));
                }
                yield PageKey.SONG_SEARCH;
            }
            case "5" -> {
                // --- 5. 발매일 필터 ---
                if (commands.length < 3) {
                    System.out.println("오류: 최소(min)와 최대(max) 날짜를 모두 입력해야 합니다.");
                } else {
                    String dateMin = commands[1].equalsIgnoreCase("NULL") ? null : commands[1];
                    String dateMax = commands[2].equalsIgnoreCase("NULL") ? null : commands[2];

                    if ((dateMin != null && !dateMin.matches("\\d{4}-\\d{2}-\\d{2}")) ||
                            (dateMax != null && !dateMax.matches("\\d{4}-\\d{2}-\\d{2}"))) {
                        System.out.println("오류: 날짜 형식이 올바르지 않습니다 (YYYY-MM-DD).");
                    } else {
                        songService.setDateMin(dateMin);
                        songService.setDateMax(dateMax);
                    }
                }
                yield PageKey.SONG_SEARCH;
            }
            case "6" -> {
                // --- 6. 정렬 기준 ---
                if (commands.length < 3) {
                    System.out.println("오류: 정렬 기준과 순서(오름차순/내림차순)를 입력해야 합니다.");
                } else {
                    String colKey = commands[1];
                    if (!SongService.getColumnMap().containsKey(colKey)) {
                        System.out.println("오류: 정렬 기준이 올바르지 않습니다. [곡명, 아티스트, 재생시간, 발매일, 제공원] 중 선택하세요.");
                    } else {
                        songService.setOrderBy(SongService.getColumnMap().get(colKey)); // 안전한 SQL 컬럼명 저장
                        songService.setOrderDir(commands[2].equalsIgnoreCase("내림차순") ? "DESC" : "ASC");
                    }
                }
                yield PageKey.SONG_SEARCH;
            }
            case "7" -> {
                songService.executeSearch();
                yield PageKey.SONG_RESULT;
            }
            case "0" -> PageKey.SEARCH; // 검색 메인 메뉴로
            default -> PageKey.SONG_SEARCH;
        };
    }

    // --- 헬퍼 메소드 (기존 코드에서 가져옴) ---

    private String[] parseKeywordAndExact(String[] args) {
        boolean exactMatch = false;
        int keywordEndIndex = args.length;
        if (args.length > 2 && !args[args.length - 1].equalsIgnoreCase("NULL") && args[args.length - 1].equalsIgnoreCase("완전일치")) {
            exactMatch = true;
            keywordEndIndex = args.length - 1;
        }
        String keyword = String.join(" ", Arrays.copyOfRange(args, 1, keywordEndIndex));
        return new String[]{keyword, String.valueOf(exactMatch)};
    }

    private Integer parseLengthToSeconds(String s) {
        if (s == null || s.equalsIgnoreCase("NULL")) return null;
        try {
            if (s.contains(":")) {
                String[] timeParts = s.split(":");
                int minutes = Integer.parseInt(timeParts[0]);
                int seconds = Integer.parseInt(timeParts[1]);
                return (minutes * 60) + seconds;
            } else {
                return Integer.parseInt(s);
            }
        } catch (NumberFormatException e) {
            System.out.println("시간 형식 오류: " + s + " (무시됨)");
            return null;
        }
    }
}