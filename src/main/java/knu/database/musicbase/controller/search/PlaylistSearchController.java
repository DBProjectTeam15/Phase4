package knu.database.musicbase.controller.search;

import knu.database.musicbase.console.PageController;
import knu.database.musicbase.console.PageKey;
import knu.database.musicbase.service.PlaylistService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlaylistSearchController implements PageController<PageKey> {

    // MainController와 같이, 필터 상태를 저장하고 관리할 Service를 주입받습니다.
    private final PlaylistService playlistService;

    /**
     * 기존 show_data()의 역할을 수행합니다.
     * Service로부터 현재 설정된 필터 값을 가져와(get) 출력합니다.
     * 또한, setupCommands()에 있던 명령어 설명(사용법)도 함께 출력합니다.
     */
    @Override
    public void displayScreen() {
        System.out.println("--- Playlist Search ---"); // super.show_data() 대체
        System.out.println("--- [현재 검색 필터] ---");

        // PlaylistService에서 현재 필터 상태를 가져온다고 가정합니다.
        // (실제로는 DTO를 사용하거나 각 필드 getter를 호출할 수 있습니다)
        // 여기서는 편의상 playlistService에 getter가 있다고 가정합니다.
        String f1 = "1. 제목: " + (playlistService.getTitleKeyword() != null ? playlistService.getTitleKeyword() + (playlistService.isTitleExact() ? " (완전일치)" : " (포함)") : "(설정 안함)");
        String f2 = "2. 악곡 수: " + (playlistService.getSongCountMin() != null ? playlistService.getSongCountMin() : "N/A") + " ~ " + (playlistService.getSongCountMax() != null ? playlistService.getSongCountMax() : "N/A");
        String f3 = "3. 댓글 수: " + (playlistService.getCommentCountMin() != null ? playlistService.getCommentCountMin() : "N/A") + " ~ " + (playlistService.getCommentCountMax() != null ? playlistService.getCommentCountMax() : "N/A");
        String f4 = "4. 소유자(닉네임): " + (playlistService.getOwnerKeyword() != null ? playlistService.getOwnerKeyword() + (playlistService.isOwnerExact() ? " (완전일치)" : " (포함)") : "(설정 안함)");
        String f5 = "5. 총 재생시간: " + (playlistService.getLengthMin() != null ? playlistService.getLengthMin() + "초" : "N/A") + " ~ " + (playlistService.getLengthMax() != null ? playlistService.getLengthMax() + "초" : "N/A");

        System.out.printf("%-45s %-45s %-45s\n", f1, f2, f3);
        System.out.printf("%-45s %-45s\n", f4, f5);
        System.out.println("-----------------------------------------------------------------------------------------------------------------");

        // setupCommands()에 있던 명령어 설명을 이곳에서 출력합니다.
        System.out.println("1. 제목 검색 (사용법: 1 [키워드] [완전일치|NULL] | 1)");
        System.out.println("2. 악곡 수 필터 (사용법: 2 [min] [max] | 2 NULL 10)");
        System.out.println("3. 댓글 수 필터 (사용법: 3 [min] [max] | 3 0 NULL)");
        System.out.println("4. 소유자 필터 (사용법: 4 [닉네임] [완전일치|NULL] | 4 NULL)");
        System.out.println("5. 총 재생시간 필터 (초/분:초) (사용법: 5 [min] [max] | 5 NULL 1800)");
        System.out.println("6. 검색하기");
        System.out.println("0. 돌아가기");
    }

    /**
     * 기존 setupCommands()에 등록되던 람다 로직을 수행합니다.
     * commands[0] (명령어 번호)에 따라 switch-case로 분기합니다.
     * 필터 값을 변경할 때, 이 클래스의 변수 대신 Service의 상태를 업데이트(set)합니다.
     * 로직 실행 후, 다음으로 이동할 PageKey를 반환합니다.
     */
    @Override
    public PageKey invoke(String[] commands) {
        return switch (commands[0]) {
            case "1" -> {
                // --- 1. 검색 실행 ---
                if (commands.length == 1) {
                    // Service의 상태를 업데이트
                    playlistService.setTitleFilter(null, false);
                } else {
                    String[] keywordParts = parseKeywordAndExact(commands);
                    playlistService.setTitleFilter(keywordParts[0], Boolean.parseBoolean(keywordParts[1]));
                }

                // Service의 검색 로직을 호출 (필요시)


                // 결과 페이지로 이동 (executeSearch() 대체)
                yield PageKey.PLAYLIST_SEARCH;
            }
            case "2" -> {
                // --- 2. 악곡 수 필터 ---
                if (commands.length < 3) {
                    System.out.println("오류: 최소(min)와 최대(max) 값을 모두 입력해야 합니다. (예: 2 10 50 / 2 NULL 20)");
                    yield PageKey.PLAYLIST_SEARCH; // 현재 페이지 유지 (fail_command + open_page)
                }
                playlistService.setSongCountFilter(parseInteger(commands[1]), parseInteger(commands[2]));
                yield PageKey.PLAYLIST_SEARCH; // 현재 페이지 새로고침 (open_page)
            }
            case "3" -> {
                // --- 3. 댓글 수 필터 ---
                if (commands.length < 3) {
                    System.out.println("오류: 최소(min)와 최대(max) 값을 모두 입력해야 합니다.");
                    yield PageKey.PLAYLIST_SEARCH;
                }
                playlistService.setCommentCountFilter(parseInteger(commands[1]), parseInteger(commands[2]));
                yield PageKey.PLAYLIST_SEARCH; // (open_page)
            }
            case "4" -> {
                // --- 4. 소유자 필터 ---
                if (commands.length < 2 || commands[1].equalsIgnoreCase("NULL")) {
                    playlistService.setOwnerFilter(null, false);
                } else {
                    String[] parts = parseKeywordAndExact(commands);
                    playlistService.setOwnerFilter(parts[0], Boolean.parseBoolean(parts[1]));
                }
                yield PageKey.PLAYLIST_SEARCH; // (open_page)
            }
            case "5" -> {
                // --- 5. 총 재생시간 필터 ---
                if (commands.length < 3) {
                    System.out.println("오류: 최소(min)와 최대(max) 값을 모두 입력해야 합니다.");
                    yield PageKey.PLAYLIST_SEARCH;
                }
                playlistService.setLengthFilter(parseLengthToSeconds(commands[1]), parseLengthToSeconds(commands[2]));
                yield PageKey.PLAYLIST_SEARCH; // (open_page)
            }
            case "6" -> {
                playlistService.executeSearch();
                yield PageKey.PLAYLIST_SEARCH_RESULT;
            }
            case "0" -> PageKey.SEARCH; // 돌아가기 (return_page)
            default -> PageKey.PLAYLIST_SEARCH; // 잘못된 입력 시 현재 페이지 유지
        };
    }

    // --- 기존 코드에 있던 Helper Methods ---
    // 이 메소드들은 private으로 이 컨트롤러 내부에 두거나,
    // 여러 곳에서 사용된다면 별도의 유틸리티 클래스로 분리할 수 있습니다.

    private String[] parseKeywordAndExact(String[] args) {
        String keyword = args[1];
        boolean exact = false;
        if (args.length > 2 && args[2].equalsIgnoreCase("완전일치")) {
            exact = true;
        }
        // "NULL" 키워드 처리는 case 1, 4에서 이미 처리됨
        return new String[]{keyword, String.valueOf(exact)};
    }

    private Integer parseInteger(String input) {
        if (input == null || input.equalsIgnoreCase("NULL") || input.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("'" + input + "'는 유효한 숫자가 아닙니다. NULL로 처리됩니다.");
            return null;
        }
    }

    private Integer parseLengthToSeconds(String input) {
        if (input == null || input.equalsIgnoreCase("NULL") || input.isBlank()) {
            return null;
        }
        try {
            // "분:초" 형식 (예: 3:30)
            if (input.contains(":")) {
                String[] parts = input.split(":");
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);
                return (minutes * 60) + seconds;
            } else {
                // "초" 형식 (예: 210)
                return Integer.parseInt(input);
            }
        } catch (Exception e) {
            System.out.println("'" + input + "'는 유효한 시간 형식이 아닙니다. (예: 180 또는 3:00) NULL로 처리됩니다.");
            return null;
        }
    }
}
