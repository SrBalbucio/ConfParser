package balbucio.confparser;

import lombok.Getter;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class ConfConfiguration {

    private static final Pattern pattern = Pattern.compile("^([ \\t]*)(#?)[ \\t]*([a-zA-Z0-9_.-]+)[ \\t]*=[ \\t]*([^#\\n\\r]*)([ \\t]*#?.*)?$");

    @Getter
    private final List<String> lines = new ArrayList<>();

    public ConfConfiguration(InputStream is) throws IOException {
        load(new BufferedReader(new InputStreamReader(is)));
    }

    public ConfConfiguration(Reader reader) throws IOException {
        load(new BufferedReader(reader));
    }

    public ConfConfiguration(String content) throws IOException {
        load(new BufferedReader(new StringReader(content)));
    }

    public ConfConfiguration(Path path) throws IOException {
        load(Files.newBufferedReader(path));
    }

    public ConfConfiguration(File file) throws IOException {
        this(file.toPath());
    }

    private void load(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
    }

    public String get(String key) {
        for (String line : lines) {
            Matcher m = pattern.matcher(line);
            if (m.matches() && m.group(2).isEmpty() && m.group(3).equals(key)) {
                return m.group(4).trim();
            }
        }
        return null;
    }

    public String getIncludingCommented(String key) {
        for (String line : lines) {
            Matcher m = pattern.matcher(line);
            if (m.matches() && m.group(3).equals(key)) {
                return m.group(4).trim();
            }
        }
        return null;
    }

    public boolean isCommented(String key) {
        for (String line : lines) {
            Matcher m = pattern.matcher(line);
            if (m.matches() && m.group(3).equals(key)) {
                return !m.group(2).isEmpty();
            }
        }
        return false;
    }

    public Map<String, String> getAll() {
        Map<String, String> map = new LinkedHashMap<>();
        for (String line : lines) {
            Matcher m = pattern.matcher(line);
            if (m.matches() && m.group(2).isEmpty()) {
                map.put(m.group(3), m.group(4).trim());
            }
        }
        return map;
    }

    public void set(String key, String newValue) {
        for (int i = 0; i < lines.size(); i++) {
            Matcher m = pattern.matcher(lines.get(i));
            if (m.matches() && m.group(3).equals(key)) {
                String updated = m.group(1) + m.group(3) + " = " + newValue + (m.group(5) != null ? m.group(5) : "");
                lines.set(i, updated);
                return;
            }
        }
        lines.add(key + " = " + newValue);
    }

    public void comment(String key) {
        for (int i = 0; i < lines.size(); i++) {
            Matcher m = pattern.matcher(lines.get(i));
            if (m.matches() && m.group(3).equals(key) && m.group(2).isEmpty()) {
                String commented = m.group(1) + "#" + m.group(3) + " = " + m.group(4) + (m.group(5) != null ? m.group(5) : "");
                lines.set(i, commented);
                return;
            }
        }
    }

    public void uncomment(String key) {
        for (int i = 0; i < lines.size(); i++) {
            Matcher m = pattern.matcher(lines.get(i));
            if (m.matches() && m.group(3).equals(key) && !m.group(2).isEmpty()) {
                String uncommented = m.group(1) + m.group(3) + " = " + m.group(4) + (m.group(5) != null ? m.group(5) : "");
                lines.set(i, uncommented);
                return;
            }
        }
    }

    @Override
    public String toString() {
        return String.join("\n", lines);
    }

    public void save(Path destination) throws IOException {
        Files.write(destination, lines);
    }

    public void save(OutputStream os) throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(os);
        for (String line : lines) {
            osw.write(line);
            osw.write('\n');
        }
        osw.flush();
    }

    public InputStream toInputStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        save(baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }
}
