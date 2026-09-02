package com.carfo.contentieux.util;

/**
 * Garde-fou contre l'injection de formule dans les exports tableur (CWE-1236).
 * <p>
 * Un texte saisi par un utilisateur (nom de partie, de cabinet, lieu d'audience,
 * résumé...) peut se retrouver, une fois exporté en Excel, interprété comme une
 * formule par le tableur si sa première lettre est {@code =}, {@code +}, {@code -}
 * ou {@code @} — y compris lorsque la cellule est techniquement de type "texte"
 * dans le fichier .xlsx : de nombreux tableurs (Excel, LibreOffice Calc, Google
 * Sheets) ré-interprètent le contenu affiché à l'ouverture. Un tel champ, rempli
 * par un rôle peu privilégié (juriste) puis exporté et ouvert par un rôle plus
 * privilégié (chef de service, Direction Générale), peut ainsi déclencher une
 * formule arbitraire dans l'environnement de la victime.
 * <p>
 * Le remède standard (recommandation OWASP) est de préfixer ces valeurs d'une
 * apostrophe, qui force leur interprétation en texte littéral côté tableur sans
 * altérer la valeur affichée.
 */
public final class SpreadsheetSanitizer {

    private static final String DANGEROUS_LEADING_CHARS = "=+-@";

    private SpreadsheetSanitizer() {
    }

    /**
     * Neutralise un texte destiné à une cellule de tableur (Excel/CSV) : si la
     * valeur commence par un caractère pouvant être interprété comme un début de
     * formule, elle est préfixée d'une apostrophe. Les valeurs {@code null}
     * deviennent {@code "—"}, à l'identique du comportement historique.
     */
    public static String sanitize(Object value) {
        if (value == null) {
            return "—";
        }
        String text = value.toString();
        if (!text.isEmpty() && DANGEROUS_LEADING_CHARS.indexOf(text.charAt(0)) >= 0) {
            return "'" + text;
        }
        return text;
    }
}
