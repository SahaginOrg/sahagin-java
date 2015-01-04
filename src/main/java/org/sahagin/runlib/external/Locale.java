package org.sahagin.runlib.external;

import java.util.ArrayList;
import java.util.List;

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

    private static List<Locale> ACCEPTABLE_LOCALES = null;
    private static java.util.Locale SYSTEM_LOCALE = java.util.Locale.getDefault();
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

    public static void setSystemLocale(java.util.Locale locale) {
    	if (locale == null) {
    		throw new NullPointerException();
    	}
    	SYSTEM_LOCALE = locale;
    	setAcceptableLocales();
    }

    // first element is used first
    public static List<Locale> getAcceptableLocales() {
    	if (ACCEPTABLE_LOCALES == null) {
    		setAcceptableLocales();
    	}
    	return ACCEPTABLE_LOCALES;
    }
    
    // search locale for java.util.Locale
    private static Locale getLocale(java.util.Locale locale) {
        for (Locale lang : values()) {
            if (lang.language.equals(locale.getLanguage())
                    && lang.country.equals(locale.getCountry())) {
                return lang;
            }
        }
        return null;
    }
    
    private static void setAcceptableLocales() {
		ACCEPTABLE_LOCALES = new ArrayList<Locale>(4);
		Locale current = getLocale(SYSTEM_LOCALE);
		if (current != null) {
			ACCEPTABLE_LOCALES.add(current);
		}
		ACCEPTABLE_LOCALES.add(Locale.DEFAULT);
		if (current != Locale.EN_US) {
			ACCEPTABLE_LOCALES.add(Locale.EN_US);
		}
    }

}
