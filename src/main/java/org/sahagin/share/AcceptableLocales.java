package org.sahagin.share;

import java.util.ArrayList;
import java.util.List;

import org.sahagin.runlib.external.Locale;

public class AcceptableLocales {
	private List<Locale> locales = new ArrayList<Locale>(4);
	
    public static AcceptableLocales getInstance(Locale userLocale) {
    	AcceptableLocales result = new AcceptableLocales();
		if (userLocale != null) {
			result.locales.add(userLocale);
		}
		result.locales.add(Locale.DEFAULT);
		if (userLocale != Locale.EN_US) {
			result.locales.add(Locale.EN_US);
		}
		return result;
    }
    
    public List<Locale> getLocales() {
    	return locales;
    }

}
