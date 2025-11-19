package knu.database.musicbase.controller.search;

import knu.database.musicbase.console.PageController;
import knu.database.musicbase.console.PageKey;
import knu.database.musicbase.service.ArtistService;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ArtistSearchController implements PageController<PageKey> {

    private final ArtistService artistService;

    @Override
    public void displayScreen() {
        System.out.println("--- Artist Search ---");
        System.out.println("--- [현재 검색 필터] ---");

        String f1 = "1. 이름: " + (artistService.getNameKeyword() != null ? artistService.getNameKeyword() + (artistService.isNameExact() ? " (완전일치)" : " (포함)") : "(설정 안함)");
        String f2 = "2. 성별: " + (artistService.getGender() != null ? (artistService.getGender().equals("None") ? "성별 없음(None)" : artistService.getGender()) : "(전체)");
        String f3 = "3. 역할: " + (artistService.getRoles().isEmpty() ? "(전체)" : artistService.getRoles().stream().collect(Collectors.joining(", ")));

        System.out.printf("%-45s %-45s %-45s\n", f1, f2, f3);
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println("1. 이름 필터 (사용법: 1 [키워드] [완전일치|NULL] | 1)");
        System.out.println("2. 성별 필터 (사용법: 2 [F|M|None] | 2 NULL)");
        System.out.println("3. 역할 필터 (사용법: 3 [역할1] [역할2] | 3 NULL)");
        System.out.println("4. 검색하기");
        System.out.println("0. 돌아가기 (검색 메뉴)");
    }

    @Override
    public PageKey invoke(String[] commands) {
        return switch (commands[0]) {
            case "1" -> {
                // --- 1. 이름으로 검색 및 실행 ---
                if (commands.length == 1) {
                    artistService.setNameFilter(null, false);
                } else {
                    String[] keywordParts = parseKeywordAndExact(commands);
                    artistService.setNameFilter(keywordParts[0], Boolean.parseBoolean(keywordParts[1]));
                }

                yield PageKey.ARTIST_SEARCH; // 현재 페이지 유지
            }
            case "2" -> {
                // --- 2. 성별 필터 ---
                String gender = (commands.length < 2) ? null : commands[1];
                if (gender != null && !gender.equalsIgnoreCase("NULL") && !gender.equalsIgnoreCase("F") && !gender.equalsIgnoreCase("M") && !gender.equalsIgnoreCase("NONE")) {
                    System.out.println("오류: 성별은 F, M, None, NULL 중 하나여야 합니다.");
                } else {
                    artistService.setGenderFilter(gender);
                }
                yield PageKey.ARTIST_SEARCH; // 현재 페이지 새로고침
            }
            case "3" -> {
                // --- 3. 역할 필터 ---
                if (commands.length < 2 || commands[1].equalsIgnoreCase("NULL")) {
                    artistService.setRolesFilter(null);
                } else {
                    artistService.setRolesFilter(parseKeywords(commands));
                }
                yield PageKey.ARTIST_SEARCH; // 현재 페이지 새로고침
            }
            case "4" -> {
                artistService.executeSearch();
                yield PageKey.ARTIST_RESULT; // 결과 페이지로 이동
            }
            case "0" -> PageKey.SEARCH; // 검색 메인 메뉴로
            default -> PageKey.ARTIST_SEARCH;
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

    private List<String> parseKeywords(String[] args) {
        String combined = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (combined.contains(",")) {
            return Arrays.stream(combined.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }
        return Arrays.stream(combined.split(" "))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}