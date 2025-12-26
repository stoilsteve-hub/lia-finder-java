package com.liafinder.service;

import com.liafinder.config.AppConfig;
import com.liafinder.model.Company;
import com.liafinder.model.Profile;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OutreachService {

    public static void generateOutreach(AppConfig cfg, Company company, Profile profile) {
        String slug = company.name().toLowerCase().replaceAll("[^a-z0-9]", "_");
        Path folder = Paths.get(cfg.output().applicationsDir(), slug);

        try {
            Files.createDirectories(folder);

            // Generate Email
            Path emailPath = folder.resolve("outreach_email.txt");
            String emailBody = generateEmailBody(cfg, company, profile);
            Files.writeString(emailPath, emailBody);

            // Generate Word Doc
            Path docPath = folder.resolve("personligt_brev.docx");
            createWordDoc(docPath, company, profile);

            System.out.println("Generated outreach for " + company.name() + " at " + folder);

        } catch (IOException e) {
            System.err.println("Failed to generate outreach for " + company.name());
            e.printStackTrace();
        }
    }

    private static String generateEmailBody(AppConfig cfg, Company company, Profile profile) {
        return "Subject: LIA Request\n\nHello " + company.name() + ",\n\n" +
                "I am writing to enquire about LIA opportunities...\n" +
                // ... logic to fill details
                "\nBest details,\n" + profile.person().get("full_name");
    }

    private static void createWordDoc(Path path, Company company, Profile profile) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph title = doc.createParagraph();
            XWPFRun run = title.createRun();
            run.setBold(true);
            run.setFontSize(16);
            run.setText("Personligt Brev - " + company.name());

            XWPFParagraph p = doc.createParagraph();
            XWPFRun r = p.createRun();
            r.setText("Hej " + company.name() + ",");
            r.addBreak();
            r.setText("Jag studerar till Javautvecklare...");

            try (FileOutputStream out = new FileOutputStream(path.toFile())) {
                doc.write(out);
            }
        }
    }
}
