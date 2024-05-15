package peterfajdiga.fastdraw.prefs;

public enum SortMethod {
    Alphabetical, Usage;

    public static SortMethod fromString (String str) {
        try {
            return valueOf(str);
        } catch (Exception ex) {
            return Alphabetical;
        }
    }
}
