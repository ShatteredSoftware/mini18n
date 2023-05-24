package software.shattered.mini18n.lang;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public abstract class PluralRules {
    public static final PluralRules ChinesePluralRules = new PluralRules() {
        @Override
        public String select(double count) {
            return "other";
        }

        @Override
        public String selectOrdinal(double count) {
            return "other";
        }
    };

    public static final PluralRules EnglishPluralRules = new PluralRules() {
        @Override
        public String select(double count) {
            Pluralization pluralization = pluralizeNumber(count);
            if (pluralization.integral == 1L && pluralization.visibleDecimalDigits == 0)  {
                return "one";
            }
            return "other";
        }


        @Override
        public String selectOrdinal(double count) {
            double abs = pluralizeNumber(count).absolute;
            if (abs % 10.0 == 1.0 && abs % 100.0 != 11.0) {
                return "one";
            }
            if (abs % 10.0 == 2.0 && abs % 100.0 != 12.0) {
                return "two";
            }
            if (abs % 10.0 == 3.0 && abs % 100.0 != 13.0) {
                return "few";
            }
            return "other";
        }
    };

    public static final PluralRules FrenchPluralRules = new PluralRules() {
        @Override
        public String select(double count) {
            long integral = pluralizeNumber(count).integral;
            if (integral == 0L) {
                return "one";
            } else if (integral == 1L) {
                return "one";
            }
            return "other";
        }

        @Override
        public String selectOrdinal(double count) {
            double abs = pluralizeNumber(count).absolute;
            if (abs == 1.0) {
                return "one";
            }
            return "other";
        }
    };

    public static final PluralRules HindiPluralRules = new PluralRules() {
        @Override
        public String select(double count) {
            Pluralization pluralization = pluralizeNumber(count);
            if (pluralization.integral == 0L || pluralization.absolute == 1.0) {
                return "one";
            }
            return "other";
        }

        @Override
        public String selectOrdinal(double count) {
            double abs = pluralizeNumber(count).absolute;
            if (abs == 1.0) {
                return "one";
            }
            if (abs == 2.0 || abs == 3.0) {
                return "two";
            }
            if (abs == 4.0) {
                return "few";
            }
            if (abs == 6.0) {
                return "many";
            }
            return "other";
        }
    };

    public static final PluralRules SpanishPluralRules = new PluralRules() {
        @Override
        public String select(double count) {
            double abs = pluralizeNumber(count).absolute;
            if (abs == 1.0) {
                return "one";
            }
            return "other";
        }

        @Override
        public String selectOrdinal(double count) {
            return "other";
        }
    };

    protected static class Pluralization {
        public final double absolute;
        public final long integral;
        public final int visibleDecimalDigits;

        public Pluralization(double absolute, long integral, int visibleDecimalDigits) {
            this.absolute = absolute;
            this.integral = integral;
            this.visibleDecimalDigits = visibleDecimalDigits;
        }
    }

    protected static final NumberFormat InternalNumberFormat = NumberFormat.getNumberInstance(Locale.US);
    static {
        InternalNumberFormat.setGroupingUsed(false);
        InternalNumberFormat.setMaximumFractionDigits(Integer.MAX_VALUE);
    }

    public static PluralRules DefaultRules = EnglishPluralRules;
    private static final Map<String, PluralRules> rules = new LinkedHashMap<String, PluralRules>() {{
        put("cn", ChinesePluralRules);
        put("en", EnglishPluralRules);
        put("fr", FrenchPluralRules);
        put("hi", HindiPluralRules);
        put("es", SpanishPluralRules);
    }};

    public static void add(Locale locale, PluralRules rules) {
        PluralRules.rules.put(locale.getLanguage(), rules);
    }

    public static PluralRules forLocale(Locale locale) {
        PluralRules rules = PluralRules.rules.get(locale.getLanguage());
        if (rules == null) {
            return DefaultRules;
        }
        return rules;
    }

    public abstract String select(double count);
    public abstract String selectOrdinal(double count);
    public String selectRange(double first, double second) {
        return select(first) + "+" + select(second);
    }

    /**
     * A reimplementation of [Unicode Operands](https://unicode.org/reports/tr35/tr35-numbers.html#Operands).
     */
    protected Pluralization pluralizeNumber(double number) {
        double n = Math.abs(number);
        // Integral Part
        long i = (long) number;
        long ai = Math.abs(i);
        // Visible Decimal Digits
        String formatted = InternalNumberFormat.format(n - ai);
        // cut off the "0." if it's there
        int v =  formatted.length() > 2 ? formatted.length() - 2 : 0;

        return new Pluralization(n, (long) Math.floor(number), v);
    }
}
