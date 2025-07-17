package com.example.groupuniquestrings;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    private static final String linesFile = "/lng.txt";
    private static final String outputFile = "output.txt";

    public static void main(String[] args) {
        List<String> lines;
        var start = Instant.now();
        if (args.length != 0 && args[0] != null && !args[0].trim().isEmpty()) {
            lines = getLines(args[0]);
        } else {
            lines = getLines();
        }
        var groupedLines = groupMsisdnLines(lines);
        writeLinesToFile(groupedLines);
        var end = Instant.now();
        System.out.println("Time elapsed: " + Duration.between(start, end).toMillis() / 1000f);
    }

    private static List<String> getLines() {
        var path = Main.class.getResource(linesFile);
        List<String> lines = null;
        try {
            if (path != null) {
                lines = Files.readAllLines(Path.of(path.toURI()));
            }
        } catch (IOException e) {
            System.out.println("Failed to read file");
            System.out.println(e.getMessage());
            System.out.println(e.toString());
        } catch (Exception e) {
            System.out.println("Failed to execute instruction");
            System.out.println(e.getMessage());
        }
        return lines;
    }

    private static List<String> getLines(String path) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Path.of(path));
        } catch (IOException e) {
            System.out.println("Failed to read file");
            System.out.println(e.getMessage());
            System.out.println(e.toString());
        } catch (Exception e) {
            System.out.println("Failed to execute instruction");
            System.out.println(e.getMessage());
        }
        return lines;
    }

    private static List<Set<String>> groupMsisdnLines(List<String> msisdns) {
        if (msisdns == null) return Collections.emptyList();
        List<Set<String>> groups = new ArrayList<>();
        List<Map<String, Integer>> parts = new ArrayList<>();
        for (String msisdn : msisdns) {
            var columns = getColumnsOf(msisdn);
            Integer groupNumber = null;
            for (int i = 0; i < Math.min(parts.size(), columns.length); i++) {
                var groupNumberFromParts = parts.get(i).get(columns[i]);
                if (groupNumberFromParts != null) {
                    if (groupNumber == null) {
                        groupNumber = groupNumberFromParts;
                    } else if (!groupNumber.equals(groupNumberFromParts)) {
                        for (String line : groups.get(groupNumberFromParts)) {
                            groups.get(groupNumber).add(line);
                            apply(getColumnsOf(line), groupNumber, parts);
                        }
                        groups.set(groupNumberFromParts, new HashSet<>());
                    }
                }
            }
            if (groupNumber == null) {
                if (Arrays.stream(columns).anyMatch(s -> !s.isEmpty())) {
                    groups.add(new HashSet<>(List.of(msisdn)));
                    apply(columns, groups.size() - 1, parts);
                }
            } else {
                groups.get(groupNumber).add(msisdn);
                apply(columns, groupNumber, parts);
            }
        }
        return groups;
    }

    private static String[] getColumnsOf(String line) {
        for (int i = 1; i < line.length() - 1; i++) {
            if (line.charAt(i) == '"' && line.charAt(i - 1) != ';' && line.charAt(i + 1) != ';') {
                return new String[0];
            }
        }
        return line.replaceAll("\"", "").split(";");
    }

    private static void apply(String[] newValues, int groupNumber, List<Map<String, Integer>> parts) {
        for (int i = 0; i < newValues.length; i++) {
            if (newValues[i].isEmpty()) {
                continue;
            }
            if (i < parts.size()) {
                parts.get(i).put(newValues[i], groupNumber);
            } else {
                Map<String, Integer> map = new HashMap<>();
                map.put(newValues[i], groupNumber);
                parts.add(map);
            }
        }
    }

    private static void writeLinesToFile(List<Set<String>> groupedLines) {
        var prepareHeader = prepareHeader(groupedLines);
        try (var outputStream = new FileOutputStream(outputFile)) {
            var headerBytes = prepareHeader.getBytes(StandardCharsets.UTF_8);
            outputStream.write(headerBytes);
            sortLines(groupedLines);
            for (int i = 0; i < groupedLines.size(); i++) {
                var stringBuilder = new StringBuilder().append("Группа ").append(i + 1).append('\n');
                stringBuilder.append(prepareString(groupedLines.get(i)));
                var bytes = stringBuilder.toString().getBytes(StandardCharsets.UTF_8);
                outputStream.write(bytes);
            }
            var path = Paths.get(outputFile);
            System.out.println("Strings is written to " + path.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to write file");
            System.out.println(e.getMessage());
            System.out.println(e.toString());
        } catch (Exception e) {
            System.out.println("Failed to execute instruction");
            System.out.println(e.getMessage());
        }
    }

    private static String prepareHeader(List<Set<String>> groupedLines) {
        var resultString = new StringBuilder();
        long count = groupedLines.stream().filter(g -> g.size() > 1).count();
        return resultString.append("Количество групп с более чем одним элементом: ").append(count).append("\n").toString();
    }

    private static void sortLines(List<Set<String>> groupedLines) {
        try {
            groupedLines.sort((g1, g2) -> Integer.compare(g2.size(), g1.size()));
        } catch (UnsupportedOperationException e) {
            System.out.println("Unable to sort empty list");
        }
    }

    private static String prepareString(Set<String> lines) {
        var resultString = new StringBuilder();
        for (String line : lines) {
            resultString.append(line).append('\n');
        }
        resultString.append('\n');
        return resultString.toString();
    }
}