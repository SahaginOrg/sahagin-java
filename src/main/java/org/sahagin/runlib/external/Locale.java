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

    public static Locale getDefault() {
        return Locale.DEFAULT;
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
    
    // search locale for java.util.Locale
    public static Locale getLocale(java.util.Locale locale) {
        for (Locale lang : values()) {
            if (lang.language.equals(locale.getLanguage())
                    && lang.country.equals(locale.getCountry())) {
                return lang;
            }
        }
        return null;
    }
    
    // returns null if not Locale found
    public static Locale getSystemLocale() {
    	return getLocale(java.util.Locale.getDefault());
    }

}
