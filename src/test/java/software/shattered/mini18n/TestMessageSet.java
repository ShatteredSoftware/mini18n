package software.shattered.mini18n;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.shattered.mini18n.lang.PluralRules;
import software.shattered.mini18n.meta.RecurseMetaProcessor;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TestMessageSet {
    private static final Locale locale = Locale.forLanguageTag("TEST");
    private static final Locale otherLocale = Locale.forLanguageTag("txtw");

    @BeforeAll
    public static void setUpPluralRules() {
        PluralRules.add(locale, PluralRules.EnglishPluralRules);
        PluralRules.add(otherLocale, PluralRules.ChinesePluralRules);
    }

    @Test
    public void shouldHandleABasicMessage() {
        MessageSet set = new MessageSet();

        set.add(locale, "test-simple", "A simple test message with %variable%.");
        assertThat(set.get(locale, "test-simple"), equalTo("A simple test message with %variable%."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("variable", "content");
        }}), equalTo("A simple test message with content."));
    }

    @Test
    public void shouldAddMultipleMessages() {
        MessageSet set = new MessageSet();
        Map<String, String> newMessages = new HashMap<>();
        newMessages.put("test-other", "Another test message.");
        newMessages.put("test-again", "Another message, again.");
        set.addAll(locale, newMessages);

        assertThat(set.get(locale, "test-other"), equalTo("Another test message."));
        assertThat(set.get(locale, "test-again"), equalTo("Another message, again."));
    }

    @Test
    public void shouldHandleAMessageInMultipleLocales() {
        MessageSet set = new MessageSet();

        set.add(locale, "test-simple", "A simple test message in locale 1.");
        set.add(otherLocale, "test-simple", "A simple test message in locale 2.");

        assertThat(set.get(locale, "test-simple"), equalTo("A simple test message in locale 1."));
        assertThat(set.get(otherLocale, "test-simple"), equalTo("A simple test message in locale 2."));
    }

    @Test
    public void shouldHandleAMessageInMultipleLocalesWithCounts() {
        MessageSet set = new MessageSet();

        set.add(locale, "test-simple.one", "There is %count% item.");
        set.add(locale, "test-simple.other", "There are %count% items.");

        set.add(otherLocale, "test-simple.one", "There was %count% item.");
        set.add(otherLocale, "test-simple.other", "There were %count% items.");

        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 1);
        }}), equalTo("There is 1 item."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 2);
        }}), equalTo("There are 2 items."));

        // Note that while this is not grammatically correct, this is logically correct
        // because otherLocale is using ChinesePluralRules which only return "other".
        assertThat(set.get(otherLocale, "test-simple", new LinkedHashMap<>() {{
            put("count", 1);
        }}), equalTo("There were 1 items."));

        assertThat(set.get(otherLocale, "test-simple", new LinkedHashMap<>() {{
            put("count", 2);
        }}), equalTo("There were 2 items."));
    }

    @Test
    public void shouldHandleAMessageWithARange() {
        MessageSet set = new MessageSet();

        set.add(locale, "test-simple.one+one", "From %range_min% thing to %range_max% thing.");
        set.add(locale, "test-simple.one+other", "From %range_min% thing to %range_max% things.");
        set.add(locale, "test-simple.other+one", "From %range_min% things to %range_max% thing.");
        set.add(locale, "test-simple.other+other", "From %range_min% things to %range_max% things.");

        set.add(otherLocale, "test-simple.other+other", "From %range_min% things to %range_max% things.");

        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("range_min", 1);
            put("range_max", 1);
        }}), equalTo("From 1 thing to 1 thing."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("range_min", 1);
            put("range_max", 5);
        }}), equalTo("From 1 thing to 5 things."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("range_min", -5);
            put("range_max", 1);
        }}), equalTo("From -5 things to 1 thing."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("range_min", 4);
            put("range_max", 9);
        }}), equalTo("From 4 things to 9 things."));


        assertThat(set.get(otherLocale, "test-simple", new LinkedHashMap<>() {{
            put("range_min", 1);
            put("range_max", 1);
        }}), equalTo("From 1 things to 1 things."));
        assertThat(set.get(otherLocale, "test-simple", new LinkedHashMap<>() {{
            put("range_min", 4);
            put("range_max", 9);
        }}), equalTo("From 4 things to 9 things."));
    }

    @Test
    public void countMustBeANumberToPluralize() {
        MessageSet set = new MessageSet();

        set.add(locale, "test-simple", "Illegal count.");
        set.add(locale, "test-simple.one", "There is %count% item.");
        set.add(locale, "test-simple.other", "There are %count% items.");

        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", "1");
        }}), equalTo("Illegal count."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 2);
        }}), equalTo("There are 2 items."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 0L);
        }}), equalTo("There are 0 items."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 5d);
        }}), equalTo("There are 5.0 items."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 6);
        }}), equalTo("There are 6 items."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 12.4);
        }}), equalTo("There are 12.4 items."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 1.0);
        }}), equalTo("There is 1.0 item."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 1);
        }}), equalTo("There is 1 item."));
    }

    @Test
    public void shouldHandleAnOrdinalMessageInMultipleLocales() {
        MessageSet set = new MessageSet();

        set.add(locale, "test-simple.one", "This is the %count%st item.");
        set.add(locale, "test-simple.two", "This is the %count%nd item.");
        set.add(locale, "test-simple.few", "This is the %count%rd item.");
        set.add(locale, "test-simple.other", "This is the %count%th item.");
        set.add(otherLocale, "test-simple.other", "This is item no. %count%.");

        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 1);
            put("ordinal", true);
        }}), equalTo("This is the 1st item."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 2);
            put("ordinal", true);
        }}), equalTo("This is the 2nd item."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 3);
            put("ordinal", true);
        }}), equalTo("This is the 3rd item."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 4);
            put("ordinal", true);
        }}), equalTo("This is the 4th item."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 5);
            put("ordinal", true);
        }}), equalTo("This is the 5th item."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 10);
            put("ordinal", true);
        }}), equalTo("This is the 10th item."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 11);
            put("ordinal", true);
        }}), equalTo("This is the 11th item."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 12);
            put("ordinal", true);
        }}), equalTo("This is the 12th item."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 21);
            put("ordinal", true);
        }}), equalTo("This is the 21st item."));
        assertThat(set.get(locale, "test-simple", new LinkedHashMap<>() {{
            put("count", 22);
            put("ordinal", true);
        }}), equalTo("This is the 22nd item."));

        // Note that while this is not grammatically correct, this is logically correct
        // because otherLocale is using ChinesePluralRules which only return "other".
        assertThat(set.get(otherLocale, "test-simple", new LinkedHashMap<>() {{
            put("count", 1);
            put("ordinal", true);
        }}), equalTo("This is item no. 1."));

        assertThat(set.get(otherLocale, "test-simple", new LinkedHashMap<>() {{
            put("count", 2);
            put("ordinal", true);
        }}), equalTo("This is item no. 2."));
    }

    @Test
    public void shouldActAsARegistry() {
        assertThat(PluralRules.forLocale(locale), equalTo(PluralRules.EnglishPluralRules));
        assertThat(PluralRules.forLocale(otherLocale), equalTo(PluralRules.ChinesePluralRules));
    }

    @Test
    public void shouldObeyDefault() {
        assertThat(PluralRules.forLocale(Locale.forLanguageTag("txth")), equalTo(PluralRules.EnglishPluralRules));
        PluralRules.DefaultRules = PluralRules.HindiPluralRules;

        assertThat(PluralRules.forLocale(Locale.forLanguageTag("txth")), equalTo(PluralRules.HindiPluralRules));
    }
}
