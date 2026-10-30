package com.example.movie;

import java.util.ArrayList;
import java.util.List;

/**
 * A tiny, dependency-free JSON reader/writer tailored to the Movie Manager's
 * storage format: a JSON array of flat objects whose values are strings,
 * numbers or booleans.
 *
 * <p>This is intentionally minimal — it is <em>not</em> a general-purpose JSON
 * library. It handles exactly the structure this application writes, which
 * keeps the project free of external dependencies.
 */
final class Json {

    private Json() {
    }

    /* ----------------------------- Writing ---------------------------- */

    static String write(List<Movie> movies) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < movies.size(); i++) {
            Movie m = movies.get(i);
            sb.append("  {\n");
            field(sb, "id", m.getId());
            sb.append(",\n");
            field(sb, "title", m.getTitle());
            sb.append(",\n");
            field(sb, "director", m.getDirector());
            sb.append(",\n");
            field(sb, "genre", m.getGenre());
            sb.append(",\n");
            field(sb, "year", m.getYear());
            sb.append(",\n");
            field(sb, "rating", m.getRating());
            sb.append(",\n");
            field(sb, "watched", m.isWatched());
            sb.append("\n  }");
            if (i < movies.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]\n");
        return sb.toString();
    }

    private static void field(StringBuilder sb, String key, String value) {
        sb.append("    \"").append(key).append("\": ").append(quote(value));
    }

    private static void field(StringBuilder sb, String key, int value) {
        sb.append("    \"").append(key).append("\": ").append(value);
    }

    private static void field(StringBuilder sb, String key, double value) {
        sb.append("    \"").append(key).append("\": ").append(value);
    }

    private static void field(StringBuilder sb, String key, boolean value) {
        sb.append("    \"").append(key).append("\": ").append(value);
    }

    private static String quote(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append("\"").toString();
    }

    /* ----------------------------- Reading ---------------------------- */

    /** Parses a JSON array of flat movie objects into a list of {@link Movie}. */
    static List<Movie> read(String json) {
        List<Movie> result = new ArrayList<>();
        Parser p = new Parser(json);
        p.skipWhitespace();
        if (p.peek() != '[') {
            throw new IllegalArgumentException("Expected '[' at start of JSON array");
        }
        p.next(); // consume '['
        p.skipWhitespace();
        if (p.peek() == ']') {
            p.next();
            return result;
        }
        while (true) {
            p.skipWhitespace();
            result.add(parseObject(p));
            p.skipWhitespace();
            char c = p.next();
            if (c == ',') {
                continue;
            }
            if (c == ']') {
                break;
            }
            throw new IllegalArgumentException("Expected ',' or ']' but found '" + c + "'");
        }
        return result;
    }

    private static Movie parseObject(Parser p) {
        Movie m = new Movie();
        if (p.next() != '{') {
            throw new IllegalArgumentException("Expected '{' at start of object");
        }
        p.skipWhitespace();
        if (p.peek() == '}') {
            p.next();
            return m;
        }
        while (true) {
            p.skipWhitespace();
            String key = p.readString();
            p.skipWhitespace();
            if (p.next() != ':') {
                throw new IllegalArgumentException("Expected ':' after key '" + key + "'");
            }
            p.skipWhitespace();
            assign(m, key, p);
            p.skipWhitespace();
            char c = p.next();
            if (c == ',') {
                continue;
            }
            if (c == '}') {
                break;
            }
            throw new IllegalArgumentException("Expected ',' or '}' but found '" + c + "'");
        }
        return m;
    }

    private static void assign(Movie m, String key, Parser p) {
        switch (key) {
            case "id" -> m.setId((int) p.readNumber());
            case "title" -> m.setTitle(p.readStringOrNull());
            case "director" -> m.setDirector(p.readStringOrNull());
            case "genre" -> m.setGenre(p.readStringOrNull());
            case "year" -> m.setYear((int) p.readNumber());
            case "rating" -> m.setRating(p.readNumber());
            case "watched" -> m.setWatched(p.readBoolean());
            default -> p.skipValue(); // ignore unknown fields
        }
    }

    /** Minimal recursive-descent cursor over a JSON string. */
    private static final class Parser {
        private final String s;
        private int pos;

        Parser(String s) {
            this.s = s;
        }

        char peek() {
            return s.charAt(pos);
        }

        char next() {
            return s.charAt(pos++);
        }

        void skipWhitespace() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
                pos++;
            }
        }

        String readString() {
            if (next() != '"') {
                throw new IllegalArgumentException("Expected '\"' at start of string");
            }
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = next();
                if (c == '"') {
                    break;
                }
                if (c == '\\') {
                    char esc = next();
                    switch (esc) {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'u' -> {
                            String hex = s.substring(pos, pos + 4);
                            pos += 4;
                            sb.append((char) Integer.parseInt(hex, 16));
                        }
                        default -> throw new IllegalArgumentException("Bad escape: \\" + esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        String readStringOrNull() {
            skipWhitespace();
            if (peek() == 'n') {
                expectLiteral("null");
                return null;
            }
            return readString();
        }

        double readNumber() {
            skipWhitespace();
            int start = pos;
            while (pos < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) {
                pos++;
            }
            return Double.parseDouble(s.substring(start, pos));
        }

        boolean readBoolean() {
            skipWhitespace();
            if (peek() == 't') {
                expectLiteral("true");
                return true;
            }
            expectLiteral("false");
            return false;
        }

        void skipValue() {
            skipWhitespace();
            char c = peek();
            if (c == '"') {
                readString();
            } else if (c == 't' || c == 'f') {
                readBoolean();
            } else if (c == 'n') {
                expectLiteral("null");
            } else {
                readNumber();
            }
        }

        private void expectLiteral(String literal) {
            if (!s.startsWith(literal, pos)) {
                throw new IllegalArgumentException("Expected literal '" + literal + "'");
            }
            pos += literal.length();
        }
    }
}
