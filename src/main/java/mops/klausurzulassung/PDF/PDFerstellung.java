package mops.klausurzulassung.PDF;

import java.io.OutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFerstellung {

    public void pdfschreiben(OutputStream outputStream) throws DocumentException {
        //Initalisieren der PDF
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);

        document.open();

        //Gestaltung der PDF

        document.addTitle("KEEEY");
        document.addSubject("");
        document.addAuthor("Joshi");
        document.addKeywords("");
        document.addCreator("");


        document.close();


    }
}
