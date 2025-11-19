package knu.database.musicbase.console;

public enum ManagerPageKey {
    MANAGER_MAIN,
    PROVIDER_MANAGEMENT,
    ARTIST_MANAGEMENT,
    ARTIST_DETAILS,
    SONG_MANAGEMENT,
    REQUEST_MANAGEMENT,
    SEARCH,
    EXIT;

    public String toString() {
        return this.name();
    }

    public String getDisplayTitle() {
        return switch (this) {
            case ARTIST_MANAGEMENT -> "-- 아티스트 관리 --";
            case PROVIDER_MANAGEMENT -> "-- 제공원 관리 --";
            case ARTIST_DETAILS -> "-- 아티스트 정보 목록 --";
            case MANAGER_MAIN -> "-- 관리자 페이지 --";
            case SONG_MANAGEMENT -> "-- 음원 관리 --";
            default -> this.name();
        };
    }
}
