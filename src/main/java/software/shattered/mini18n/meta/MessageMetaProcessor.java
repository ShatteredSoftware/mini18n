package software.shattered.mini18n.meta;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

public interface MessageMetaProcessor {
    String getId();
    String parse(Locale locale, String argument, String currentMessage, @Nullable Map<String, Object> data);
}
