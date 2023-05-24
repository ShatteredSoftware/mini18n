package software.shattered.mini18n.meta;

import org.jetbrains.annotations.Nullable;
import software.shattered.mini18n.MessageSet;

import java.util.Locale;
import java.util.Map;

public class RecurseMetaProcessor implements MessageMetaProcessor {
    public final String Id = "message";
    private final MessageSet set;

    public RecurseMetaProcessor(MessageSet set) {
        this.set = set;
    }

    @Override
    public String getId() {
        return Id;
    }

    @Override
    public String parse(Locale locale, String argument, String currentMessage, @Nullable Map<String, Object> data) {
        String replacement = set.get(locale, argument, data);
        if (replacement == null) {
            return "";
        }
        return replacement;
    }
}
