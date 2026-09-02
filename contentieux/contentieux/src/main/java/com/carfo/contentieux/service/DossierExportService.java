package com.carfo.contentieux.service;

import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.AudienceDecision;
import com.carfo.contentieux.model.Document;
import com.carfo.contentieux.model.Dossier;
import com.carfo.contentieux.model.DossierCabinet;
import com.carfo.contentieux.model.DossierJuriste;
import com.carfo.contentieux.model.EtapeDossier;
import com.carfo.contentieux.model.Implication;
import com.carfo.contentieux.repository.AudienceDecisionRepository;
import com.carfo.contentieux.repository.DossierCabinetRepository;
import com.carfo.contentieux.repository.DossierJuristeRepository;
import com.carfo.contentieux.repository.ImplicationRepository;
import com.carfo.contentieux.util.SpreadsheetSanitizer;
import org.springframework.stereotype.Service;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Génère les exports Word/Excel/PDF d'un dossier (extrait de DossierController, qui ne
 * conservait auparavant que les endpoints REST : la mise en forme des documents n'a rien à
 * voir avec le routage HTTP et mérite sa propre unité, testable indépendamment).
 */
@Service
public class DossierExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FMT = NumberFormat.getIntegerInstance(Locale.FRANCE);

    private final DossierService dossierService;
    private final ImplicationRepository implicationRepository;
    private final DossierJuristeRepository dossierJuristeRepository;
    private final DossierCabinetRepository dossierCabinetRepository;
    private final AudienceDecisionRepository audienceDecisionRepository;

    public DossierExportService(DossierService dossierService,
                                 ImplicationRepository implicationRepository,
                                 DossierJuristeRepository dossierJuristeRepository,
                                 DossierCabinetRepository dossierCabinetRepository,
                                 AudienceDecisionRepository audienceDecisionRepository) {
        this.dossierService = dossierService;
        this.implicationRepository = implicationRepository;
        this.dossierJuristeRepository = dossierJuristeRepository;
        this.dossierCabinetRepository = dossierCabinetRepository;
        this.audienceDecisionRepository = audienceDecisionRepository;
    }

    private Dossier resoudreDossier(String numeroDossier) {
        return dossierService.getDossierById(numeroDossier)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable"));
    }

    // AudienceDecision se rattache desormais a EtapeDossier, pas directement a Dossier :
    // on passe par le repository plutot que par une collection portee par l'entite Dossier.
    private List<AudienceDecision> audiencesDu(Dossier d) {
        return audienceDecisionRepository.findByEtapeDossier_Dossier_NumeroDossier(d.getNumeroDossier());
    }

    public byte[] exportWord(String numeroDossier) throws Exception {
        Dossier d = resoudreDossier(numeroDossier);
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            setDocumentProperties(doc, "Fiche dossier " + numeroDossier);

            XWPFParagraph header = doc.createParagraph();
            XWPFRun hr = header.createRun();
            hr.setBold(true);
            hr.setFontSize(16);
            hr.setColor("1F3864");
            hr.setText("SIGECO — CARFO — Service Contentieux et Juridique");
            XWPFParagraph subHeader = doc.createParagraph();
            XWPFRun subHr = subHeader.createRun();
            subHr.setItalic(true);
            subHr.setFontSize(9);
            subHr.setColor("666666");
            subHr.setText("Fiche dossier générée le " + LocalDate.now().format(DATE_FMT));

            XWPFParagraph info = doc.createParagraph();
            XWPFRun infoRun = info.createRun();
            infoRun.setFontSize(12);
            infoRun.setText("Numéro Affaire: " + safeToString(d.getNumeroDossier()));
            infoRun.addBreak();
            infoRun.setText("Nature: " + humanize(d.getTypeContentieux()));
            infoRun.addBreak();
            infoRun.setText("État de la procédure: " + humanize(d.getEtapeCourante()));
            infoRun.addBreak();
            infoRun.setText("Date d'ouverture: " + formatDate(d.getDateOuverture()));

            addHeading(doc, "Résumé Affaire");
            doc.createParagraph().createRun().setText(safeToString(d.getResumeAffaire()));

            addHeading(doc, "Observation");
            doc.createParagraph().createRun().setText(safeToString(d.getObservation()));

            addHeading(doc, "Parties impliquées");
            XWPFTable partiesTable = doc.createTable();
            setTableHeader(partiesTable, Arrays.asList("Nom", "Prénom", "Rôle", "Lien"));
            List<Implication> implications = implicationRepository.findByDossier_NumeroDossier(numeroDossier);
            for (Implication imp : implications) {
                fillRow(partiesTable.createRow(), Arrays.asList(
                        safeToString(imp.getPartie() != null ? imp.getPartie().getNom() : null),
                        safeToString(imp.getPartie() != null ? imp.getPartie().getPrenom() : null),
                        humanize(imp.getRole()),
                        humanize(imp.getLienParente())));
            }

            addHeading(doc, "Suivi et acteurs");
            XWPFTable suiviTable = doc.createTable();
            setTableHeader(suiviTable, Arrays.asList("Juriste", "Matricule", "Cabinet externe", "Avocat référent"));
            List<DossierJuriste> juristes = dossierJuristeRepository.findByDossier_NumeroDossier(numeroDossier);
            List<DossierCabinet> cabinets = dossierCabinetRepository.findByDossier_NumeroDossier(numeroDossier);
            int max = Math.max(juristes.size(), cabinets.size());
            for (int i = 0; i < max; i++) {
                DossierJuriste dj = i < juristes.size() ? juristes.get(i) : null;
                DossierCabinet dc = i < cabinets.size() ? cabinets.get(i) : null;
                fillRow(suiviTable.createRow(), Arrays.asList(
                        safeToString(dj != null && dj.getJuriste() != null ? dj.getJuriste().getNom() : null),
                        safeToString(dj != null && dj.getJuriste() != null ? dj.getJuriste().getMatricule() : null),
                        safeToString(dc != null && dc.getCabinet() != null ? dc.getCabinet().getNomCabinet() : null),
                        safeToString(dc != null ? dc.getNomAvocatReferent() : null)));
            }

            addHeading(doc, "Chronologie du dossier");
            XWPFTable chronoTable = doc.createTable();
            setTableHeader(chronoTable, Arrays.asList("Étape", "Date début", "Date fin"));
            List<EtapeDossier> etapes = d.getEtapes() != null ? d.getEtapes() : Collections.emptyList();
            for (EtapeDossier e : etapes) {
                fillRow(chronoTable.createRow(), Arrays.asList(
                        humanize(e.getEtape()),
                        formatDate(e.getDateDebut()),
                        e.getDateFin() == null ? "En cours" : formatDate(e.getDateFin())));
            }

            addHeading(doc, "Audiences et décisions");
            XWPFTable audTable = doc.createTable();
            setTableHeader(audTable, Arrays.asList("Date", "Lieu", "Type", "Nature décision", "Résumé", "Issue", "Obtenu", "Dû", "Frais"));
            List<AudienceDecision> audiences = audiencesDu(d);
            for (AudienceDecision a : audiences) {
                fillRow(audTable.createRow(), Arrays.asList(
                        formatDate(a.getDate()),
                        safeToString(a.getLieuAudience()),
                        humanize(a.getTypeEtape()),
                        humanize(a.getNatureDecision()),
                        safeToString(a.getResumeDecision()),
                        humanize(a.getIssuePourCarfo()),
                        formatCurrencyObj(a.getMontantObtenu()),
                        formatCurrencyObj(a.getMontantDu()),
                        formatCurrencyObj(a.getFraisJustice())));
            }

            addHeading(doc, "Documents associés");
            XWPFTable docsTable = doc.createTable();
            setTableHeader(docsTable, Arrays.asList("Type document", "Date d'ajout"));
            List<Document> docs = d.getDocuments() != null ? d.getDocuments() : Collections.emptyList();
            for (Document docObj : docs) {
                fillRow(docsTable.createRow(), Arrays.asList(
                        humanize(docObj.getTypeDocument()),
                        formatDate(docObj.getDateAjout())));
            }

            addHeading(doc, "Synthèse financière");
            XWPFTable synthTable = doc.createTable();
            setTableHeader(synthTable, Arrays.asList("Libellé", "Valeur"));
            XWPFTableRow r1 = synthTable.createRow();
            setTableCell(r1.getCell(0), "Montant réclamé");
            setTableCell(r1.getCell(1), formatCurrencyObj(d.getMontantReclame()));
            XWPFTableRow r2 = synthTable.createRow();
            setTableCell(r2.getCell(0), "Frais de justice cumulés");
            setTableCell(r2.getCell(1), formatCurrencyObj(d.getFraisJustice()));
            XWPFTableRow r3 = synthTable.createRow();
            setTableCell(r3.getCell(0), "Risque financier");
            setTableCell(r3.getCell(1), formatCurrencyObj(d.getRisqueFinancier()));

            doc.write(baos);
            return baos.toByteArray();
        }
    }

    public byte[] exportExcel(String numeroDossier) throws Exception {
        Dossier d = resoudreDossier(numeroDossier);
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            CreationHelper createHelper = wb.getCreationHelper();
            Sheet sheet = wb.createSheet("Fiche dossier");

            CellStyle headerStyle = wb.createCellStyle();
            Font hfont = wb.createFont();
            hfont.setBold(true);
            headerStyle.setFont(hfont);

            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));

            CellStyle currencyStyle = wb.createCellStyle();
            currencyStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0 \"FCFA\""));

            // Texte long (résumé de décision, lieu d'audience) : retour à la ligne automatique
            // plutôt qu'une colonne étirée à l'infini par autoSizeColumn.
            CellStyle wrapStyle = wb.createCellStyle();
            wrapStyle.setWrapText(true);
            wrapStyle.setVerticalAlignment(VerticalAlignment.TOP);

            int rowIdx = 0;
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue("Numéro Affaire");
            row.createCell(1).setCellValue(SpreadsheetSanitizer.sanitize(d.getNumeroDossier()));
            row.getCell(0).setCellStyle(headerStyle);
            row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue("Nature");
            row.createCell(1).setCellValue(humanize(d.getTypeContentieux()));
            row.getCell(0).setCellStyle(headerStyle);
            row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue("État de la procédure");
            row.createCell(1).setCellValue(humanize(d.getEtapeCourante()));
            row.getCell(0).setCellStyle(headerStyle);
            row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue("Date d'ouverture");
            row.getCell(0).setCellStyle(headerStyle);
            Cell c = row.createCell(1);
            setCellDate(c, d.getDateOuverture(), dateStyle);

            rowIdx++;
            row = sheet.createRow(rowIdx++);
            int col = 0;
            row.createCell(col++).setCellValue("Nom");
            row.createCell(col++).setCellValue("Prénom");
            row.createCell(col++).setCellValue("Rôle");
            row.createCell(col++).setCellValue("Lien");
            for (Cell hc : row) hc.setCellStyle(headerStyle);
            for (Implication imp : implicationRepository.findByDossier_NumeroDossier(numeroDossier)) {
                row = sheet.createRow(rowIdx++);
                col = 0;
                row.createCell(col++).setCellValue(SpreadsheetSanitizer.sanitize(imp.getPartie() != null ? imp.getPartie().getNom() : null));
                row.createCell(col++).setCellValue(SpreadsheetSanitizer.sanitize(imp.getPartie() != null ? imp.getPartie().getPrenom() : null));
                row.createCell(col++).setCellValue(humanize(imp.getRole()));
                row.createCell(col++).setCellValue(humanize(imp.getLienParente()));
            }

            rowIdx++;
            row = sheet.createRow(rowIdx++);
            col = 0;
            row.createCell(col++).setCellValue("Juriste");
            row.createCell(col++).setCellValue("Matricule");
            row.createCell(col++).setCellValue("Cabinet externe");
            row.createCell(col++).setCellValue("Avocat référent");
            for (Cell hc : row) hc.setCellStyle(headerStyle);
            List<DossierJuriste> sheetJuristes = dossierJuristeRepository.findByDossier_NumeroDossier(numeroDossier);
            List<DossierCabinet> sheetCabinets = dossierCabinetRepository.findByDossier_NumeroDossier(numeroDossier);
            int maxActeurs = Math.max(sheetJuristes.size(), sheetCabinets.size());
            for (int i = 0; i < maxActeurs; i++) {
                row = sheet.createRow(rowIdx++);
                col = 0;
                DossierJuriste dj = i < sheetJuristes.size() ? sheetJuristes.get(i) : null;
                DossierCabinet dc = i < sheetCabinets.size() ? sheetCabinets.get(i) : null;
                row.createCell(col++).setCellValue(SpreadsheetSanitizer.sanitize(dj != null && dj.getJuriste() != null ? dj.getJuriste().getNom() : null));
                row.createCell(col++).setCellValue(SpreadsheetSanitizer.sanitize(dj != null && dj.getJuriste() != null ? dj.getJuriste().getMatricule() : null));
                row.createCell(col++).setCellValue(SpreadsheetSanitizer.sanitize(dc != null && dc.getCabinet() != null ? dc.getCabinet().getNomCabinet() : null));
                row.createCell(col++).setCellValue(SpreadsheetSanitizer.sanitize(dc != null ? dc.getNomAvocatReferent() : null));
            }

            rowIdx++;
            row = sheet.createRow(rowIdx++);
            col = 0;
            row.createCell(col++).setCellValue("Étape");
            row.createCell(col++).setCellValue("Date début");
            row.createCell(col++).setCellValue("Date fin");
            for (Cell hc : row) hc.setCellStyle(headerStyle);
            List<EtapeDossier> sheetEtapes = d.getEtapes() != null ? d.getEtapes() : Collections.emptyList();
            for (EtapeDossier e : sheetEtapes) {
                row = sheet.createRow(rowIdx++);
                col = 0;
                row.createCell(col++).setCellValue(humanize(e.getEtape()));
                setCellDate(row.createCell(col++), e.getDateDebut(), dateStyle);
                if (e.getDateFin() != null) {
                    setCellDate(row.createCell(col++), e.getDateFin(), dateStyle);
                } else {
                    row.createCell(col++).setCellValue("En cours");
                }
            }

            rowIdx++;
            row = sheet.createRow(rowIdx++);
            col = 0;
            row.createCell(col++).setCellValue("Type document");
            row.createCell(col++).setCellValue("Date d'ajout");
            for (Cell hc : row) hc.setCellStyle(headerStyle);
            List<Document> sheetDocs = d.getDocuments() != null ? d.getDocuments() : Collections.emptyList();
            for (Document docObj : sheetDocs) {
                row = sheet.createRow(rowIdx++);
                col = 0;
                row.createCell(col++).setCellValue(humanize(docObj.getTypeDocument()));
                if (docObj.getDateAjout() != null) {
                    setCellDate(row.createCell(col++), docObj.getDateAjout(), dateStyle);
                } else {
                    row.createCell(col++).setCellValue("—");
                }
            }

            rowIdx++;
            row = sheet.createRow(rowIdx++);
            col = 0;
            row.createCell(col++).setCellValue("Date");
            row.createCell(col++).setCellValue("Lieu");
            row.createCell(col++).setCellValue("Type");
            row.createCell(col++).setCellValue("Nature décision");
            row.createCell(col++).setCellValue("Résumé");
            row.createCell(col++).setCellValue("Issue");
            row.createCell(col++).setCellValue("Obtenu");
            row.createCell(col++).setCellValue("Dû");
            row.createCell(col++).setCellValue("Frais");
            for (Cell hc : row) hc.setCellStyle(headerStyle);

            List<AudienceDecision> sheetAudiences = audiencesDu(d);
            for (AudienceDecision a : sheetAudiences) {
                row = sheet.createRow(rowIdx++);
                col = 0;
                setCellDate(row.createCell(col++), a.getDate(), dateStyle);
                Cell lieuCell = row.createCell(col++);
                lieuCell.setCellValue(SpreadsheetSanitizer.sanitize(a.getLieuAudience()));
                lieuCell.setCellStyle(wrapStyle);
                row.createCell(col++).setCellValue(humanize(a.getTypeEtape()));
                row.createCell(col++).setCellValue(humanize(a.getNatureDecision()));
                Cell resumeCell = row.createCell(col++);
                resumeCell.setCellValue(SpreadsheetSanitizer.sanitize(a.getResumeDecision()));
                resumeCell.setCellStyle(wrapStyle);
                row.createCell(col++).setCellValue(humanize(a.getIssuePourCarfo()));
                setCellCurrencyNumeric(row.createCell(col++), a.getMontantObtenu(), currencyStyle);
                setCellCurrencyNumeric(row.createCell(col++), a.getMontantDu(), currencyStyle);
                setCellCurrencyNumeric(row.createCell(col++), a.getFraisJustice(), currencyStyle);
            }

            for (int i = 0; i < 12; i++) sheet.autoSizeColumn(i);
            // Le résumé de décision peut être long : on l'auto-dimensionne comme les autres colonnes
            // (pour les cas courts), puis on plafonne sa largeur pour éviter une colonne démesurée —
            // le retour à la ligne (wrapStyle) prend le relais au-delà de ce plafond.
            final int largeurMaxColonneTexte = 60 * 256; // ~60 caractères, unité POI = 1/256e de caractère
            if (sheet.getColumnWidth(4) > largeurMaxColonneTexte) {
                sheet.setColumnWidth(4, largeurMaxColonneTexte);
            }
            setWorkbookProperties(wb, "Fiche dossier " + numeroDossier);
            wb.write(baos);
            return baos.toByteArray();
        }
    }

    public byte[] exportPdf(String numeroDossier) throws Exception {
        Dossier d = resoudreDossier(numeroDossier);
        try {
            try (PDDocument doc = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                doc.getDocumentInformation().setTitle("Fiche dossier " + numeroDossier);
                doc.getDocumentInformation().setAuthor("CARFO — Service Contentieux et Juridique");
                PDRectangle landscape = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
                PDPage page = new PDPage(landscape);
                doc.addPage(page);
                PDType0Font font = chargerPolice(doc);

                PdfCursor c = new PdfCursor();
                c.doc = doc;
                c.page = page;
                c.font = font;
                c.margin = 40;
                c.cs = new PDPageContentStream(doc, page);
                c.y = page.getMediaBox().getHeight() - c.margin;

                drawText(c, "SIGECO — CARFO — Service Contentieux et Juridique", 14, true);
                drawText(c, "Fiche dossier générée le " + LocalDate.now().format(DATE_FMT), 8, false);
                c.y -= 6;
                drawText(c, "Numéro Affaire: " + safeToString(d.getNumeroDossier()), 10, false);
                drawText(c, "Nature: " + humanize(d.getTypeContentieux()) + " — État: " + humanize(d.getEtapeCourante())
                        + " — Date ouverture: " + formatDate(d.getDateOuverture()), 10, false);
                c.y -= 10;

                drawText(c, "Résumé Affaire", 11, true);
                drawWrapped(c, safeToString(d.getResumeAffaire()), 9, (int) (page.getMediaBox().getWidth() - 2 * c.margin));
                c.y -= 6;
                drawText(c, "Observation", 11, true);
                drawWrapped(c, safeToString(d.getObservation()), 9, (int) (page.getMediaBox().getWidth() - 2 * c.margin));
                c.y -= 10;

                List<Implication> pdfImplications = implicationRepository.findByDossier_NumeroDossier(numeroDossier);
                drawTable(c, "Parties impliquées",
                        new String[]{"Nom", "Prénom", "Rôle", "Lien"},
                        new float[]{130, 130, 100, 130},
                        pdfImplications.stream().map(imp -> new String[]{
                                safeToString(imp.getPartie() != null ? imp.getPartie().getNom() : null),
                                safeToString(imp.getPartie() != null ? imp.getPartie().getPrenom() : null),
                                humanize(imp.getRole()),
                                humanize(imp.getLienParente())
                        }).collect(java.util.stream.Collectors.toList()));

                List<DossierJuriste> pdfJuristes = dossierJuristeRepository.findByDossier_NumeroDossier(numeroDossier);
                List<DossierCabinet> pdfCabinets = dossierCabinetRepository.findByDossier_NumeroDossier(numeroDossier);
                int maxActeurs = Math.max(pdfJuristes.size(), pdfCabinets.size());
                List<String[]> acteurRows = new ArrayList<>();
                for (int i = 0; i < maxActeurs; i++) {
                    DossierJuriste dj = i < pdfJuristes.size() ? pdfJuristes.get(i) : null;
                    DossierCabinet dc = i < pdfCabinets.size() ? pdfCabinets.get(i) : null;
                    acteurRows.add(new String[]{
                            safeToString(dj != null && dj.getJuriste() != null ? dj.getJuriste().getNom() : null),
                            safeToString(dj != null && dj.getJuriste() != null ? dj.getJuriste().getMatricule() : null),
                            safeToString(dc != null && dc.getCabinet() != null ? dc.getCabinet().getNomCabinet() : null),
                            safeToString(dc != null ? dc.getNomAvocatReferent() : null)
                    });
                }
                drawTable(c, "Suivi et acteurs", new String[]{"Juriste", "Matricule", "Cabinet externe", "Avocat référent"},
                        new float[]{130, 100, 130, 130}, acteurRows);

                List<EtapeDossier> pdfEtapes = d.getEtapes() != null ? d.getEtapes() : Collections.emptyList();
                drawTable(c, "Chronologie du dossier", new String[]{"Étape", "Date début", "Date fin"},
                        new float[]{150, 100, 100},
                        pdfEtapes.stream().map(e -> new String[]{
                                humanize(e.getEtape()), formatDate(e.getDateDebut()),
                                e.getDateFin() == null ? "En cours" : formatDate(e.getDateFin())
                        }).collect(java.util.stream.Collectors.toList()));

                List<Document> pdfDocs = d.getDocuments() != null ? d.getDocuments() : Collections.emptyList();
                drawTable(c, "Documents associés", new String[]{"Type document", "Date d'ajout"},
                        new float[]{200, 120},
                        pdfDocs.stream().map(docObj -> new String[]{
                                humanize(docObj.getTypeDocument()), formatDate(docObj.getDateAjout())
                        }).collect(java.util.stream.Collectors.toList()));

                List<AudienceDecision> audiences = audiencesDu(d);
                drawTable(c, "Audiences et décisions",
                        new String[]{"Date", "Lieu", "Type", "Nature", "Résumé", "Issue", "Obtenu", "Dû", "Frais"},
                        new float[]{60, 90, 65, 65, 180, 70, 65, 65, 65},
                        audiences.stream().map(a -> new String[]{
                                formatDate(a.getDate()), safeToString(a.getLieuAudience()), humanize(a.getTypeEtape()),
                                humanize(a.getNatureDecision()), safeToString(a.getResumeDecision()), humanize(a.getIssuePourCarfo()),
                                formatCurrencyObj(a.getMontantObtenu()), formatCurrencyObj(a.getMontantDu()), formatCurrencyObj(a.getFraisJustice())
                        }).collect(java.util.stream.Collectors.toList()));

                drawTable(c, "Synthèse financière", new String[]{"Libellé", "Valeur"}, new float[]{200, 150},
                        Arrays.asList(
                                new String[]{"Montant réclamé", formatCurrencyObj(d.getMontantReclame())},
                                new String[]{"Frais de justice cumulés", formatCurrencyObj(d.getFraisJustice())},
                                new String[]{"Risque financier", formatCurrencyObj(d.getRisqueFinancier())}));

                c.cs.close();
                doc.save(baos);
                return baos.toByteArray();
            }
        } catch (Exception ex) {
            journaliserErreurPdf(numeroDossier, ex);
            throw ex;
        }
    }

    private static void journaliserErreurPdf(String numeroDossier, Exception ex) {
        try {
            java.nio.file.Path logdir = java.nio.file.Paths.get("exports");
            if (!java.nio.file.Files.exists(logdir)) java.nio.file.Files.createDirectories(logdir);
            java.nio.file.Path f = logdir.resolve("pdf-error.log");
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String content = java.time.LocalDateTime.now() + " - Exception during PDF export for " + numeroDossier
                    + System.lineSeparator() + sw + System.lineSeparator();
            java.nio.file.Files.write(f, content.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ioex) {
            // ignore secondary failures
        }
    }

    private static PDType0Font chargerPolice(PDDocument doc) {
        PDType0Font font = null;
        try (InputStream fontIs = DossierExportService.class.getResourceAsStream("/fonts/DejaVuSerif.ttf")) {
            if (fontIs != null) {
                font = PDType0Font.load(doc, fontIs);
            }
        } catch (Exception ex) {
            // ignore and try other fallbacks
        }
        if (font == null) {
            // Secours specifique a un poste de developpement Windows : absent sur les
            // environnements Linux (dont le conteneur Docker de production), donc ignore
            // silencieusement la-bas pour passer au dernier secours ci-dessous.
            try {
                String windir = System.getenv("windir");
                if (windir != null) {
                    java.nio.file.Path sysf = java.nio.file.Paths.get(windir, "Fonts", "arial.ttf");
                    if (java.nio.file.Files.exists(sysf)) {
                        try (InputStream s = java.nio.file.Files.newInputStream(sysf)) {
                            font = PDType0Font.load(doc, s);
                        }
                    }
                }
            } catch (Exception ex) { /* ignore */ }
        }
        if (font == null) {
            // Police toujours presente dans le jar PDFBox lui-meme (LiberationSans-Regular.ttf),
            // donc ce secours fonctionne de maniere identique quel que soit l'OS hote.
            try (InputStream s = PDType0Font.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf")) {
                if (s != null) {
                    font = PDType0Font.load(doc, s);
                }
            } catch (Exception ignored) { /* ignore */ }
        }
        if (font == null) {
            throw new IllegalStateException("Impossible de charger une police pour l'export PDF (aucun des secours n'a fonctionne)");
        }
        return font;
    }

    // ------------------ Helpers de mise en forme ------------------

    private static String formatDate(Object o) {
        if (o == null) return "—";
        if (o instanceof LocalDate) return ((LocalDate) o).format(DATE_FMT);
        return safeToString(o);
    }

    private static String humanize(Object o) {
        if (o == null) return "—";
        try {
            for (String getter : new String[]{"getLibelle", "getLabel", "getNature", "getNom", "getName"}) {
                try {
                    Method m = o.getClass().getMethod(getter);
                    Object r = m.invoke(o);
                    if (r != null) return humanize(r.toString());
                } catch (NoSuchMethodException ns) {
                    // ignore
                }
            }
        } catch (Exception ex) {
            // fallthrough
        }
        String s = o.toString();
        s = s.replace('_', ' ');
        s = s.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
        s = s.trim().replaceAll("\\s+", " ");
        StringBuilder out = new StringBuilder();
        for (String part : s.split(" ")) {
            if (part.isEmpty()) continue;
            out.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) out.append(part.substring(1).toLowerCase());
            out.append(' ');
        }
        return out.toString().trim();
    }

    private static String formatCurrencyObj(Object o) {
        if (o == null) return "—";
        try {
            if (o instanceof BigDecimal) return CURRENCY_FMT.format(((BigDecimal) o).longValue()) + " FCFA";
            if (o instanceof Number) return CURRENCY_FMT.format(((Number) o).longValue()) + " FCFA";
            return o.toString();
        } catch (Exception ex) {
            return o.toString();
        }
    }

    private static void setTableHeader(XWPFTable t, List<String> headers) {
        XWPFTableRow header = t.getRow(0);
        header.getCell(0).setText(headers.get(0));
        for (int i = 1; i < headers.size(); i++) header.addNewTableCell().setText(headers.get(i));
    }

    private static void setTableCell(XWPFTableCell cell, String text) {
        if (cell == null) return;
        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();
        p.createRun().setText(text == null ? "—" : text);
    }

    // A bare XWPFDocument has no "Heading1"/"Heading2" style definitions, so paragraph.setStyle("HeadingN")
    // is a silent no-op (renders as plain body text). Apply direct character formatting instead.
    private static void addHeading(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(200);
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(13);
        r.setColor("1F3864");
        r.setText(text);
    }

    private static void setDocumentProperties(XWPFDocument doc, String title) {
        org.apache.poi.ooxml.POIXMLProperties.CoreProperties props = doc.getProperties().getCoreProperties();
        props.setTitle(title);
        props.setCreator("CARFO — Service Contentieux et Juridique");
    }

    private static void setWorkbookProperties(XSSFWorkbook wb, String title) {
        org.apache.poi.ooxml.POIXMLProperties.CoreProperties props = wb.getProperties().getCoreProperties();
        props.setTitle(title);
        props.setCreator("CARFO — Service Contentieux et Juridique");
    }

    // XWPFTable.createRow() copies the cell count of the previous row (e.g. the header),
    // so cells 0..n-1 already exist: reuse them instead of calling addNewTableCell(),
    // which would otherwise append extra cells beyond the copied ones.
    private static void fillRow(XWPFTableRow row, List<String> values) {
        for (int i = 0; i < values.size(); i++) {
            XWPFTableCell cell = i < row.getTableCells().size() ? row.getCell(i) : row.addNewTableCell();
            setTableCell(cell, values.get(i));
        }
    }

    private static String safeToString(Object o) {
        return o == null ? "—" : o.toString();
    }

    private static void setCellDate(Cell cell, Object value, CellStyle dateStyle) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        if (value instanceof LocalDate) {
            cell.setCellValue(java.sql.Date.valueOf((LocalDate) value));
            cell.setCellStyle(dateStyle);
            return;
        }
        cell.setCellValue(value.toString());
    }

    private static void setCellCurrencyNumeric(Cell cell, Object value, CellStyle style) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        try {
            if (value instanceof BigDecimal) {
                cell.setCellValue(((BigDecimal) value).doubleValue());
                cell.setCellStyle(style);
                return;
            }
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
                cell.setCellStyle(style);
                return;
            }
            cell.setCellValue(value.toString());
        } catch (Exception e) {
            cell.setCellValue(value.toString());
        }
    }

    // very simple text wrapper based on char count and font size heuristic
    private static List<String> wrapText(String text, int maxWidthPx, PDType0Font font, int fontSize) {
        if (text == null) return Collections.singletonList("-");
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder cur = new StringBuilder();
        for (String w : words) {
            String candidate = cur.length() == 0 ? w : cur + " " + w;
            if (candidate.length() * 6 > maxWidthPx && cur.length() > 0) {
                lines.add(cur.toString());
                cur = new StringBuilder(w);
            } else {
                cur = new StringBuilder(candidate);
            }
        }
        if (cur.length() > 0) lines.add(cur.toString());
        return lines;
    }

    // ------------------ Helpers de mise en page PDF ------------------
    // Curseur mutable partagé pour que drawText/drawWrapped/drawTable declenchent chacun un
    // saut de page (nouvelle PDPage + flux de contenu) de facon transparente, sans dupliquer
    // la logique de pagination a chaque site d'appel.
    private static class PdfCursor {
        PDDocument doc;
        PDPage page;
        PDPageContentStream cs;
        PDType0Font font;
        float y;
        float margin;
    }

    private static void newPage(PdfCursor c) throws IOException {
        c.cs.close();
        c.page = new PDPage(c.page.getMediaBox());
        c.doc.addPage(c.page);
        c.cs = new PDPageContentStream(c.doc, c.page);
        c.y = c.page.getMediaBox().getHeight() - c.margin;
    }

    private static void ensureSpace(PdfCursor c, float needed) throws IOException {
        if (c.y - needed < c.margin) {
            newPage(c);
        }
    }

    private static void drawText(PdfCursor c, String text, int fontSize, boolean emphasis) throws IOException {
        ensureSpace(c, fontSize + 6);
        c.cs.beginText();
        c.cs.setFont(c.font, fontSize);
        if (emphasis) {
            c.cs.setStrokingColor(java.awt.Color.BLACK);
            c.cs.setLineWidth(0.6f);
            c.cs.setRenderingMode(org.apache.pdfbox.pdmodel.graphics.state.RenderingMode.FILL_STROKE);
        }
        c.cs.newLineAtOffset(c.margin, c.y);
        c.cs.showText(text == null || text.isBlank() ? "—" : text);
        c.cs.endText();
        if (emphasis) {
            c.cs.beginText();
            c.cs.setRenderingMode(org.apache.pdfbox.pdmodel.graphics.state.RenderingMode.FILL);
            c.cs.endText();
        }
        c.y -= fontSize + 6;
    }

    private static void drawWrapped(PdfCursor c, String text, int fontSize, int maxWidthPx) throws IOException {
        for (String line : wrapText(text, maxWidthPx, c.font, fontSize)) {
            ensureSpace(c, fontSize + 4);
            c.cs.beginText();
            c.cs.setFont(c.font, fontSize);
            c.cs.newLineAtOffset(c.margin, c.y);
            c.cs.showText(line);
            c.cs.endText();
            c.y -= fontSize + 4;
        }
    }

    private static void drawTable(PdfCursor c, String title, String[] headers, float[] colWidths, List<String[]> rows) throws IOException {
        drawText(c, title, 11, true);
        float tableWidth = 0;
        for (float w : colWidths) tableWidth += w;
        int fontSize = 8;
        float headerRowHeight = fontSize + 6;
        ensureSpace(c, headerRowHeight);
        drawTableRow(c, headers, colWidths, fontSize, true);
        if (rows.isEmpty()) {
            drawWrapped(c, "Aucune donnée.", fontSize, (int) tableWidth);
        }
        for (String[] row : rows) {
            List<List<String>> wrapped = new ArrayList<>();
            int maxLines = 1;
            for (int i = 0; i < row.length; i++) {
                List<String> lines = wrapText(row[i] == null ? "—" : row[i], (int) colWidths[i] - 4, c.font, fontSize);
                wrapped.add(lines);
                maxLines = Math.max(maxLines, lines.size());
            }
            float rowHeight = maxLines * (fontSize + 3) + 4;
            ensureSpace(c, rowHeight);
            drawWrappedTableRow(c, wrapped, colWidths, rowHeight);
        }
        c.y -= 10;
    }

    private static void drawTableRow(PdfCursor c, String[] cells, float[] colWidths, int fontSize, boolean header) throws IOException {
        float rowHeight = fontSize + 6;
        float tableWidth = 0;
        for (float w : colWidths) tableWidth += w;
        if (header) {
            c.cs.setNonStrokingColor(new java.awt.Color(230, 230, 230));
            c.cs.addRect(c.margin, c.y - rowHeight, tableWidth, rowHeight);
            c.cs.fill();
            c.cs.setNonStrokingColor(java.awt.Color.BLACK);
        }
        float x = c.margin + 2;
        float textY = c.y - rowHeight + 4;
        for (int i = 0; i < cells.length; i++) {
            c.cs.beginText();
            c.cs.setFont(c.font, fontSize);
            c.cs.newLineAtOffset(x, textY);
            c.cs.showText(cells[i] == null ? "—" : cells[i]);
            c.cs.endText();
            x += colWidths[i];
        }
        c.cs.setStrokingColor(java.awt.Color.LIGHT_GRAY);
        c.cs.moveTo(c.margin, c.y - rowHeight);
        c.cs.lineTo(c.margin + tableWidth, c.y - rowHeight);
        c.cs.stroke();
        c.y -= rowHeight;
    }

    private static void drawWrappedTableRow(PdfCursor c, List<List<String>> wrappedCells, float[] colWidths, float rowHeight) throws IOException {
        float tableWidth = 0;
        for (float w : colWidths) tableWidth += w;
        float x = c.margin + 2;
        for (int i = 0; i < wrappedCells.size(); i++) {
            float lineY = c.y - c.font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000f * 8 - 2;
            lineY = c.y - 10;
            for (String line : wrappedCells.get(i)) {
                c.cs.beginText();
                c.cs.setFont(c.font, 8);
                c.cs.newLineAtOffset(x, lineY);
                c.cs.showText(line);
                c.cs.endText();
                lineY -= 11;
            }
            x += colWidths[i];
        }
        c.cs.setStrokingColor(java.awt.Color.LIGHT_GRAY);
        c.cs.moveTo(c.margin, c.y - rowHeight);
        c.cs.lineTo(c.margin + tableWidth, c.y - rowHeight);
        c.cs.stroke();
        c.y -= rowHeight;
    }
}
