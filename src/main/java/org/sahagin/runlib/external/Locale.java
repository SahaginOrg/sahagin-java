package org.sahagin.runlib.external;

public enum Locale {

    DEFAULT("default", "DEFAULT"),

    AR_SA("ar", "SA"),

    DE_DE("de", "DE"),

    EN_US("en", "US"),

    ES_ES("es", "ES"),

    FR_FR("fr", "FR"),

    IT_IT("it", "IT"),

    JA_JP("ja", "JP"),

    KO_KR("ko", "KR"),

    SV_SE("sv", "SE"),

    ZH_CN("zh", "CN"),

    ZH_TW("zh", "TW");

    private String language;
    private String country;

    private Locale(String language, String country) {
        this.language = language;
        this.country = country;
    }

    public String getValue() {
        return language + "-" + country;
    }

    public String getLanguage() {
        return language;
    }

    public String getCountry() {
        return country;
    }

    public static Locale getEnum(String value) {
        for (Locale locale : values()) {
            if (locale.getValue().equals(value)) {
                return locale;
            }
        }
        return null;
    }

    public static Locale getClosestLanguageLocale(java.util.Locale locale) {
        // search exactly matched locale
        for (Locale lang : values()) {
            if (lang.language.equals(locale.getLanguage())
                    && lang.country.equals(locale.getCountry())) {
                return lang;
            }
        }

        // search language matched locale
        for (Locale lang : values()) {
            if (lang.language.equals(locale.getLanguage())) {
                return lang;
            }
        }

        return Locale.DEFAULT;
    }

}
