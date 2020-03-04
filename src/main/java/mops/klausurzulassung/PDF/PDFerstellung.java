package mops.klausurzulassung.PDF;

import java.io.OutputStream;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFerstellung {

    public static void pdfschreiben(OutputStream outputStream, String quittung, String matr, String fach) throws DocumentException {

        //Initalisieren der PDF
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);

        document.open();

        //Gestaltung der PDF

        document.addTitle("Quittung");
        document.addSubject("Quittung");
        document.addAuthor("Uni-Duesseldorf");

        Paragraph paragraph = new Paragraph();
        paragraph.add(new Chunk("Matrikelnummer: " + matr + "\n" + "Modul: " + fach));
        document.add(paragraph);

        //Hinzufügen des QR-Codes
        // -> pending

        paragraph = new Paragraph();
        paragraph.add(quittung + "\n");
        document.add(paragraph);

        document.close();


    }
}
