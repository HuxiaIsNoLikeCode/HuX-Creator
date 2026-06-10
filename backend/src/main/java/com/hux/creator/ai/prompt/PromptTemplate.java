package com.hux.creator.ai.prompt;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
public class PromptTemplate {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");

    private String template;

    public String render(Map<String, String> variables) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = variables.getOrDefault(key, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
