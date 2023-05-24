package software.shattered.mini18n.meta;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.shattered.mini18n.MessageSet;
import software.shattered.mini18n.lang.PluralRules;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TestRecurseMetaProcessor {
    private static final Locale locale = Locale.forLanguageTag("TEST");
    private static final Locale otherLocale = Locale.forLanguageTag("TEST2");

    @BeforeAll
    public static void setUpPluralRules() {
        PluralRules.add(locale, PluralRules.EnglishPluralRules);
        PluralRules.add(otherLocale, PluralRules.ChinesePluralRules);
    }

    @Test
    public void shouldHandleARecursiveMessage() {
        MessageSet set = new MessageSet();
        set.addMetaProcessor(new RecurseMetaProcessor(set));

        set.add(locale, "test-simple", "%message:prefix% This is a message.");
        set.add(locale, "prefix", "[Prefix]");

        assertThat(set.get(locale, "test-simple"), equalTo("[Prefix] This is a message."));
    }

    @Test
    public void shouldHandleMultipleRecursiveMessages() {
        MessageSet set = new MessageSet();
        set.addMetaProcessor(new RecurseMetaProcessor(set));

        set.add(locale, "test-simple", "%message:prefix% This is a message %message:content%!");
        set.add(locale, "prefix", "[Prefix]");
        set.add(locale, "content", "with content");

        assertThat(set.get(locale, "test-simple"), equalTo("[Prefix] This is a message with content!"));
    }

    @Test
    public void shouldEmptyNonexistentPlaceholders() {
        MessageSet set = new MessageSet();
        set.addMetaProcessor(new RecurseMetaProcessor(set));

        set.add(locale, "test-simple", "%message:prefix% This is a message!");
        set.add(locale, "prefix", "[%message:prefix-content%]");

        assertThat(set.get(locale, "test-simple"), equalTo("[] This is a message!"));
    }

    @Test
    public void shouldHandleADeepRecursiveMessage() {
        MessageSet set = new MessageSet();
        set.addMetaProcessor(new RecurseMetaProcessor(set));

        set.add(locale, "test-simple", "%message:prefix% This is a message!");
        set.add(locale, "prefix", "[%message:prefix-content%]");
        set.add(locale, "prefix-content", "Prefix");

        assertThat(set.get(locale, "test-simple"), equalTo("[Prefix] This is a message!"));
    }
}
