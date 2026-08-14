package com.carfo.contentieux.util;

import java.time.LocalDate;

/**
 * Garde-fou anti-saisie erronée : rejette les dates manifestement absurdes
 * (faute de frappe sur l'année, etc.) sans imposer de règle métier précise
 * sur les délais de procédure.
 */
public final class DateGuard {

    private static final int MAX_YEARS_IN_PAST = 40;
    private static final int MAX_YEARS_IN_FUTURE = 5;

    private DateGuard() {
    }

    public static void checkReasonable(LocalDate date, String champ) {
        if (date == null) {
            return;
        }
        LocalDate min = LocalDate.now().minusYears(MAX_YEARS_IN_PAST);
        LocalDate max = LocalDate.now().plusYears(MAX_YEARS_IN_FUTURE);
        if (date.isBefore(min) || date.isAfter(max)) {
            throw new IllegalArgumentException(
                    "Le champ '" + champ + "' contient une date invraisemblable (" + date
                            + "). Elle doit être comprise entre " + min + " et " + max + ".");
        }
    }

    public static void checkOrder(LocalDate debut, LocalDate fin, String champDebut, String champFin) {
        if (debut != null && fin != null && fin.isBefore(debut)) {
            throw new IllegalArgumentException(
                    "Le champ '" + champFin + "' (" + fin + ") ne peut pas être antérieur à '" + champDebut + "' (" + debut + ").");
        }
    }
}
