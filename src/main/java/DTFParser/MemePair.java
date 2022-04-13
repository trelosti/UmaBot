package DTFParser;

class MemePair {
    String title;
    String src;

    static MemePair makeMemePair(String title, String src) {
        return new MemePair(title, src);
    }

    private MemePair(String title, String src) {
        this.title = title;
        this.src = src;
    }
}
