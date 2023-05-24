package software.shattered.mini18n;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.shattered.mini18n.lang.PluralRules;
import software.shattered.mini18n.meta.MessageMetaProcessor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageSet {
    private static final Pattern MetaRegex = Pattern.compile("%(.*?):(.*?)%");
    private final Map<Locale, Map<String, String>> messages = new LinkedHashMap<>();
    private final Map<String, MessageMetaProcessor> metaProcessors = new LinkedHashMap<>();

    public void add(Locale locale, String id, String message) {
        getSafeLocale(locale).put(id, message);
    }

    public void addAll(Locale locale, Map<String, String> messages) {
        getSafeLocale(locale).putAll(messages);
    }

    public void addMetaProcessor(MessageMetaProcessor processor) {
        this.metaProcessors.put(processor.getId(), processor);
    }

    public String get(Locale locale, String message) {
        return get(locale, message, null);
    }
    public String get(Locale locale, String message, @Nullable Map<String, Object> data) {
        String countSpecifier = "";
        if (data != null) {
            Number count = getFromData(data, "count", Number.class);
            boolean ordinal = getFromData(data, "ordinal", Boolean.class, false);

            PluralRules rules = PluralRules.forLocale(locale);

            if (count != null) {
                if (ordinal) {
                    countSpecifier = "." + rules.selectOrdinal(count.doubleValue());
                }
                else {
                    countSpecifier = "." + rules.select(count.doubleValue());
                }
            }

            Number rangeMin = getFromData(data, "range_min", Number.class);
            Number rangeMax = getFromData(data, "range_max", Number.class);
            if (rangeMin != null && rangeMax != null) {
                countSpecifier = "." + rules.selectRange(rangeMin.doubleValue(), rangeMax.doubleValue());
            }
        }

        Map<String, String> localeMap = getSafeLocale(locale);
        String rawMessage = localeMap.get(message + countSpecifier);
        if (rawMessage == null) {
            return null;
        }
        Matcher matcher = MetaRegex.matcher(rawMessage);
        while (matcher.find()) {
            String meta = matcher.group(1);
            String result = matcher.group(2);
            MessageMetaProcessor processor = metaProcessors.get(meta);
            String replacement = "";
            if (processor != null) {
                replacement = processor.parse(locale, result, rawMessage, data);
            }
            rawMessage = matcher.replaceFirst(replacement);
            matcher = MetaRegex.matcher(rawMessage);
        }


        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                rawMessage = rawMessage.replace("%" + entry.getKey() + "%", entry.getValue().toString());
            }
        }
        return rawMessage;
    }


    private <T> @Nullable T getFromData(@NotNull Map<String, Object> data, @NotNull String key, Class<T> tClass) {
        return getFromData(data, key, tClass, null);
    }
    private <T> T getFromData(@NotNull Map<String, Object> data, @NotNull String key, Class<T> tClass, T def) {
        if (!data.containsKey(key)) {
            return def;
        }
        Object value = data.get(key);
        if (tClass.isInstance(value)) {
            //noinspection unchecked -- checked above
            return (T) value;
        }
        return def;
    }

    private Map<String, String> getSafeLocale(Locale locale) {
        if (messages.containsKey(locale)) {
            return messages.get(locale);
        }
        Map<String, String> localeMap = new LinkedHashMap<>();
        messages.put(locale, localeMap);
        return localeMap;
    }
}