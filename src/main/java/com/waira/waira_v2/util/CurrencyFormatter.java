package com.waira.waira_v2.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component("currencyFormatter")
public class CurrencyFormatter {

    private static final Locale LOCALE_CO = Locale.forLanguageTag("es-CO");
    private final DecimalFormat decimalFormat;

    public CurrencyFormatter() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(LOCALE_CO);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        this.decimalFormat = new DecimalFormat("#,##0", symbols);
    }

    public String cop(Double value) {
        if (value == null) {
            return "Precio no definido";
        }
        return "COP " + decimalFormat.format(value);
    }
}
