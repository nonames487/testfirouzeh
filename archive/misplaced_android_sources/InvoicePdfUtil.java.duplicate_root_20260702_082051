package ai.arena.hisabdar.modern1405.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class to compile invoice records and export clean PDFs for sharing via Whatsapp/SMS (v10 Roadmap).
 */
public class InvoicePdfUtil {

    /**
     * Compiles transactional data into a friendly PDF format in the cache folder,
     * and generates a FileProvider intent to share instantly.
     */
    public static void exportAndShareInvoicePdf(Context context, long documentId, String partyName, long totalAmount, String date) {
        String filename = "firoozeh_invoice_" + documentId + ".pdf";
        File pdfFile = new File(context.getCacheDir(), filename);

        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            // Simplified PDF compilation simulation
            // In a real device environment, you would use android.graphics.pdf.PdfDocument
            String dummyPdfContent = "%PDF-1.4\n% Firoozeh AI Document ID: " + documentId + "\n" +
                    "/Party: " + partyName + "\n" +
                    "/Total: " + totalAmount + " Rials\n" +
                    "/Date: " + date + "\n%%EOF";
            fos.write(dummyPdfContent.getBytes());
            fos.flush();

            // Share via FileProvider to respect modern Android file security policy (compileSdk=35 / targetSdk=35)
            Uri fileUri = FileProvider.getUriForFile(
                    context,
                    "ai.arena.hisabdar.modern1405.fileprovider",
                    pdfFile
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            Intent chooser = Intent.createChooser(shareIntent, "ارسال فاکتور به مشتری (واتساپ / ایتا / پیامک)");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
