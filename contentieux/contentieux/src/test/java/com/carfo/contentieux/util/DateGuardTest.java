package com.carfo.contentieux.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateGuardTest {

    @Test
    void accepteUneDatePlausible() {
        assertDoesNotThrow(() -> DateGuard.checkReasonable(LocalDate.now(), "dateOuverture"));
        assertDoesNotThrow(() -> DateGuard.checkReasonable(LocalDate.now().minusYears(5), "dateOuverture"));
    }

    @Test
    void accepteUneDateNulle() {
        // Un champ optionnel non renseigné ne doit pas être rejeté par le garde-fou.
        assertDoesNotThrow(() -> DateGuard.checkReasonable(null, "dateFin"));
    }

    @Test
    void rejetteUneDateTropAncienne() {
        assertThrows(IllegalArgumentException.class,
                () -> DateGuard.checkReasonable(LocalDate.of(1500, 1, 1), "dateOuverture"));
    }

    @Test
    void rejetteUneDateTropLointaine() {
        assertThrows(IllegalArgumentException.class,
                () -> DateGuard.checkReasonable(LocalDate.of(3000, 1, 1), "dateOuverture"));
    }

    @Test
    void accepteUnOrdreCoherent() {
        assertDoesNotThrow(() -> DateGuard.checkOrder(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), "dateDebut", "dateFin"));
    }

    @Test
    void rejetteUneDateFinAnterieureALaDateDebut() {
        assertThrows(IllegalArgumentException.class, () -> DateGuard.checkOrder(
                LocalDate.of(2026, 8, 20), LocalDate.of(2026, 1, 1), "dateDebut", "dateFin"));
    }

    @Test
    void accepteUneDateFinAbsente() {
        assertDoesNotThrow(() -> DateGuard.checkOrder(LocalDate.of(2026, 8, 20), null, "dateDebut", "dateFin"));
    }
}
