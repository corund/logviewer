package net.corund.logviewer;

public class Utils {
    private Utils() {
    }
    
    public static String join(String...args) {
        StringBuilder sb = new StringBuilder();
        for (String s : args) {
            sb.append(s);
        }
        return sb.toString();
    }
}
