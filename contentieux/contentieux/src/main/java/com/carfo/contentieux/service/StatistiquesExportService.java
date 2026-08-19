package com.carfo.contentieux.service;

import com.carfo.contentieux.dto.StatistiquesSynthese;
import com.carfo.contentieux.model.EtapeDossier;
import com.carfo.contentieux.model.TypeContentieux;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Génère les exports Word/Excel/PDF de la synthèse statistique (extrait de
 * StatistiquesController, cf. DossierExportService pour la même logique côté dossiers).
 */
@Service
public class StatistiquesExportService {

    private static final java.text.NumberFormat CURRENCY_FMT = java.text.NumberFormat.getIntegerInstance(Locale.FRANCE);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static String money(BigDecimal v) {
        return v == null ? "0 FCFA" : CURRENCY_FMT.format(v.longValue()) + " FCFA";
    }

    private static String humanizeEnum(Object e) {
        if (e == null) return "—";
        String s = e.toString().replace('_', ' ');
        StringBuilder out = new StringBuilder();
        for (String part : s.split(" ")) {
            if (part.isEmpty()) continue;
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase()).append(' ');
        }
        return out.toString().trim();
    }

    public byte[] generatePdf(StatistiquesSynthese s) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            doc.getDocumentInformation().setTitle("Synthèse des statistiques — Service Contentieux et Juridique");
            doc.getDocumentInformation().setAuthor("CARFO — Service Contentieux et Juridique");

            PDType0Font[] fontRegularHolder = {null};
            PDType0Font[] fontBoldHolder = {null};
            try {
                File f1 = new File("C:\\Windows\\Fonts\\arial.ttf");
                File f2 = new File("C:\\Windows\\Fonts\\arialbd.ttf");
                if (f1.exists()) { try (FileInputStream is = new FileInputStream(f1)) { fontRegularHolder[0] = PDType0Font.load(doc, is); } }
                if (f2.exists()) { try (FileInputStream is = new FileInputStream(f2)) { fontBoldHolder[0] = PDType0Font.load(doc, is); } }
            } catch (Exception ex) { /* fall through to fallbacks below */ }
            if (fontRegularHolder[0] == null) {
                InputStream fallback = PDType0Font.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSerif-Regular.ttf");
                if (fallback != null) fontRegularHolder[0] = PDType0Font.load(doc, fallback);
            }
            if (fontBoldHolder[0] == null) fontBoldHolder[0] = fontRegularHolder[0];
            final PDType0Font fontRegular = fontRegularHolder[0];
            final PDType0Font fontBold = fontBoldHolder[0];

            PDPage page = new PDPage();
            doc.addPage(page);
            float margin = 50;
            float[] y = {page.getMediaBox().getHeight() - margin};
            PDPageContentStream[] csHolder = {new PDPageContentStream(doc, page)};

            java.util.function.BiConsumer<String, Object[]> line = (fmt, args) -> {
                try {
                    if (y[0] < margin + 20) {
                        csHolder[0].close();
                        PDPage p = new PDPage();
                        doc.addPage(p);
                        csHolder[0] = new PDPageContentStream(doc, p);
                        y[0] = p.getMediaBox().getHeight() - margin;
                    }
                    csHolder[0].beginText();
                    csHolder[0].setFont(fontRegular, 10);
                    csHolder[0].newLineAtOffset(margin, y[0]);
                    csHolder[0].showText(String.format(fmt, args));
                    csHolder[0].endText();
                    y[0] -= 15;
                } catch (IOException ex) { throw new RuntimeException(ex); }
            };

            PDPageContentStream cs = csHolder[0];
            cs.beginText(); cs.setFont(fontBold, 15); cs.newLineAtOffset(margin, y[0]); cs.showText("SIGECO — CARFO — Synthèse des statistiques"); cs.endText();
            y[0] -= 18;
            cs.beginText(); cs.setFont(fontRegular, 9); cs.newLineAtOffset(margin, y[0]); cs.showText("Généré le " + LocalDate.now().format(DATE_FMT)); cs.endText();
            y[0] -= 24;

            line.accept("Total dossiers : %d", new Object[]{s.getTotalDossiers()});
            line.accept("Dossiers ouverts : %d", new Object[]{s.getDossiersOuverts()});
            line.accept("Dossiers en appel : %d", new Object[]{s.getDossiersEnAppel()});
            line.accept("Dossiers en cassation : %d", new Object[]{s.getDossiersEnCassation()});
            line.accept("Alertes urgentes : %d", new Object[]{s.getDossiersAlerte()});
            line.accept("Risque financier cumulé : %s", new Object[]{money(s.getRisqueFinancierCumule())});
            line.accept("Frais de justice cumulés : %s", new Object[]{money(s.getFraisJusticeCumules())});
            line.accept("Montant réclamé cumulé : %s", new Object[]{money(s.getMontantReclameCumule())});
            y[0] -= 10;

            csHolder[0].beginText(); csHolder[0].setFont(fontBold, 12); csHolder[0].newLineAtOffset(margin, y[0]); csHolder[0].showText("Répartition par type de contentieux"); csHolder[0].endText();
            y[0] -= 18;
            if (s.getRepartitionParType() != null) {
                for (Map.Entry<TypeContentieux.Nature, Long> e : s.getRepartitionParType().entrySet()) {
                    line.accept("  %s : %d", new Object[]{humanizeEnum(e.getKey()), e.getValue()});
                }
            }
            y[0] -= 6;

            csHolder[0].beginText(); csHolder[0].setFont(fontBold, 12); csHolder[0].newLineAtOffset(margin, y[0]); csHolder[0].showText("Répartition par étape actuelle"); csHolder[0].endText();
            y[0] -= 18;
            if (s.getRepartitionParEtape() != null) {
                for (Map.Entry<EtapeDossier.Etape, Long> e : s.getRepartitionParEtape().entrySet()) {
                    line.accept("  %s : %d", new Object[]{humanizeEnum(e.getKey()), e.getValue()});
                }
            }
            y[0] -= 6;

            csHolder[0].beginText(); csHolder[0].setFont(fontBold, 12); csHolder[0].newLineAtOffset(margin, y[0]); csHolder[0].showText("Top 5 — Dossiers les plus risqués"); csHolder[0].endText();
            y[0] -= 18;
            if (s.getTop5Risques() != null) {
                for (StatistiquesSynthese.DossierLeger dl : s.getTop5Risques()) {
                    line.accept("  %s — %s — %s — %s", new Object[]{
                            dl.getNumeroDossier(), humanizeEnum(dl.getTypeContentieux()),
                            money(dl.getRisqueFinancier()), humanizeEnum(dl.getEtapeCourante())});
                }
            }

            csHolder[0].close();
            doc.save(baos);
        }
        return baos.toByteArray();
    }

    public byte[] generateExcel(StatistiquesSynthese s) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            org.apache.poi.ooxml.POIXMLProperties.CoreProperties props = wb.getProperties().getCoreProperties();
            props.setTitle("Synthèse des statistiques"); props.setCreator("CARFO — Service Contentieux et Juridique");

            CellStyle headerStyle = wb.createCellStyle();
            Font hfont = wb.createFont(); hfont.setBold(true); headerStyle.setFont(hfont);

            Sheet sheet = wb.createSheet("Synthese");
            int r = 0;

            r = kv(sheet, r, "Total dossiers", String.valueOf(s.getTotalDossiers()), headerStyle);
            r = kv(sheet, r, "Dossiers ouverts", String.valueOf(s.getDossiersOuverts()), headerStyle);
            r = kv(sheet, r, "Dossiers en appel", String.valueOf(s.getDossiersEnAppel()), headerStyle);
            r = kv(sheet, r, "Dossiers en cassation", String.valueOf(s.getDossiersEnCassation()), headerStyle);
            r = kv(sheet, r, "Alertes urgentes", String.valueOf(s.getDossiersAlerte()), headerStyle);
            r = kv(sheet, r, "Risque financier cumulé", money(s.getRisqueFinancierCumule()), headerStyle);
            r = kv(sheet, r, "Frais de justice cumulés", money(s.getFraisJusticeCumules()), headerStyle);
            r = kv(sheet, r, "Montant réclamé cumulé", money(s.getMontantReclameCumule()), headerStyle);
            r++;

            Row typeHeader = sheet.createRow(r++);
            typeHeader.createCell(0).setCellValue("Répartition par type de contentieux");
            typeHeader.getCell(0).setCellStyle(headerStyle);
            if (s.getRepartitionParType() != null) {
                for (Map.Entry<TypeContentieux.Nature, Long> e : s.getRepartitionParType().entrySet()) {
                    Row row = sheet.createRow(r++);
                    row.createCell(0).setCellValue(humanizeEnum(e.getKey()));
                    row.createCell(1).setCellValue(e.getValue());
                }
            }
            r++;

            Row etapeHeader = sheet.createRow(r++);
            etapeHeader.createCell(0).setCellValue("Répartition par étape actuelle");
            etapeHeader.getCell(0).setCellStyle(headerStyle);
            if (s.getRepartitionParEtape() != null) {
                for (Map.Entry<EtapeDossier.Etape, Long> e : s.getRepartitionParEtape().entrySet()) {
                    Row row = sheet.createRow(r++);
                    row.createCell(0).setCellValue(humanizeEnum(e.getKey()));
                    row.createCell(1).setCellValue(e.getValue());
                }
            }
            r++;

            Row top5Title = sheet.createRow(r++);
            top5Title.createCell(0).setCellValue("Top 5 dossiers à risque");
            top5Title.getCell(0).setCellStyle(headerStyle);

            Row top5Header = sheet.createRow(r++);
            top5Header.createCell(0).setCellValue("N° dossier");
            top5Header.createCell(1).setCellValue("Type");
            top5Header.createCell(2).setCellValue("Risque financier");
            top5Header.createCell(3).setCellValue("Étape");
            for (Cell hc : top5Header) hc.setCellStyle(headerStyle);

            if (s.getTop5Risques() != null) {
                for (StatistiquesSynthese.DossierLeger dl : s.getTop5Risques()) {
                    Row row = sheet.createRow(r++);
                    row.createCell(0).setCellValue(dl.getNumeroDossier());
                    row.createCell(1).setCellValue(humanizeEnum(dl.getTypeContentieux()));
                    row.createCell(2).setCellValue(dl.getRisqueFinancier() != null ? dl.getRisqueFinancier().doubleValue() : 0.0);
                    row.createCell(3).setCellValue(humanizeEnum(dl.getEtapeCourante()));
                }
            }

            for (int i = 0; i < 4; i++) sheet.autoSizeColumn(i);
            wb.write(baos);
            return baos.toByteArray();
        }
    }

    private static int kv(Sheet sheet, int r, String label, String value, CellStyle headerStyle) {
        Row row = sheet.createRow(r);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
        row.getCell(0).setCellStyle(headerStyle);
        return r + 1;
    }

    public byte[] generateWord(StatistiquesSynthese s) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            org.apache.poi.ooxml.POIXMLProperties.CoreProperties props = doc.getProperties().getCoreProperties();
            props.setTitle("Synthèse des statistiques"); props.setCreator("CARFO — Service Contentieux et Juridique");

            XWPFParagraph title = doc.createParagraph();
            XWPFRun titleRun = title.createRun();
            titleRun.setBold(true); titleRun.setFontSize(16); titleRun.setColor("1F3864");
            titleRun.setText("SIGECO — CARFO — Synthèse des statistiques");

            XWPFParagraph sub = doc.createParagraph();
            XWPFRun subRun = sub.createRun();
            subRun.setItalic(true); subRun.setFontSize(9); subRun.setColor("666666");
            subRun.setText("Généré le " + LocalDate.now().format(DATE_FMT));

            heading(doc, "Indicateurs clés");
            XWPFTable kpi = doc.createTable();
            fillKvRow(kpi.getRow(0), "Total dossiers", String.valueOf(s.getTotalDossiers()));
            fillKvRow(kpi.createRow(), "Dossiers ouverts", String.valueOf(s.getDossiersOuverts()));
            fillKvRow(kpi.createRow(), "Dossiers en appel", String.valueOf(s.getDossiersEnAppel()));
            fillKvRow(kpi.createRow(), "Dossiers en cassation", String.valueOf(s.getDossiersEnCassation()));
            fillKvRow(kpi.createRow(), "Alertes urgentes", String.valueOf(s.getDossiersAlerte()));
            fillKvRow(kpi.createRow(), "Risque financier cumulé", money(s.getRisqueFinancierCumule()));
            fillKvRow(kpi.createRow(), "Frais de justice cumulés", money(s.getFraisJusticeCumules()));
            fillKvRow(kpi.createRow(), "Montant réclamé cumulé", money(s.getMontantReclameCumule()));

            heading(doc, "Répartition par type de contentieux");
            XWPFTable typeTable = doc.createTable();
            typeTable.getRow(0).getCell(0).setText("Type");
            typeTable.getRow(0).addNewTableCell().setText("Nombre");
            if (s.getRepartitionParType() != null) {
                for (Map.Entry<TypeContentieux.Nature, Long> e : s.getRepartitionParType().entrySet()) {
                    fillKvRow(typeTable.createRow(), humanizeEnum(e.getKey()), String.valueOf(e.getValue()));
                }
            }

            heading(doc, "Répartition par étape actuelle");
            XWPFTable etapeTable = doc.createTable();
            etapeTable.getRow(0).getCell(0).setText("Étape");
            etapeTable.getRow(0).addNewTableCell().setText("Nombre");
            if (s.getRepartitionParEtape() != null) {
                for (Map.Entry<EtapeDossier.Etape, Long> e : s.getRepartitionParEtape().entrySet()) {
                    fillKvRow(etapeTable.createRow(), humanizeEnum(e.getKey()), String.valueOf(e.getValue()));
                }
            }

            heading(doc, "Top 5 — Dossiers les plus risqués");
            XWPFTable top5 = doc.createTable();
            String[] top5Headers = {"N° dossier", "Type", "Risque financier", "Étape"};
            top5.getRow(0).getCell(0).setText(top5Headers[0]);
            for (int i = 1; i < top5Headers.length; i++) top5.getRow(0).addNewTableCell().setText(top5Headers[i]);
            if (s.getTop5Risques() != null) {
                for (StatistiquesSynthese.DossierLeger dl : s.getTop5Risques()) {
                    XWPFTableRow row = top5.createRow();
                    String[] vals = {dl.getNumeroDossier(), humanizeEnum(dl.getTypeContentieux()), money(dl.getRisqueFinancier()), humanizeEnum(dl.getEtapeCourante())};
                    for (int i = 0; i < vals.length; i++) {
                        XWPFTableCell cell = i < row.getTableCells().size() ? row.getCell(i) : row.addNewTableCell();
                        cell.setText(vals[i]);
                    }
                }
            }

            doc.write(baos);
            return baos.toByteArray();
        }
    }

    private static void heading(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(200);
        XWPFRun r = p.createRun();
        r.setBold(true); r.setFontSize(13); r.setColor("1F3864");
        r.setText(text);
    }

    private static void fillKvRow(XWPFTableRow row, String label, String value) {
        row.getCell(0).setText(label);
        XWPFTableCell valueCell = row.getTableCells().size() > 1 ? row.getCell(1) : row.addNewTableCell();
        valueCell.setText(value);
    }
}
