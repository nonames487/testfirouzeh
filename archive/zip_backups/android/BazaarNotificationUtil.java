package ai.arena.hisabdar.modern1405.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Nekte 4: Advanced Merchant Toolkit.
 * Automatically formats polite, bazzar-friendly, empathetic reminders for debtors,
 * and handles custom routing intents via WhatsApp, Telegram, SMS, or Iranian bazzar apps (Eitaa, Bale).
 */
public class BazaarNotificationUtil {

    public enum Platform {
        SMS,
        WHATSAPP,
        TELEGRAM,
        EITAA,
        BALE
    }

    /**
     * Compiles a highly-customized, polite bazaar remind text.
     * Bazaar culture values reputation (آبرو) and courtesy; reminders must be gentle yet clear.
     */
    public static String formatBazaarDebtReminder(String merchantName, String debtorName, long amount, int delayDays) {
        return "سلام و برکت خدمت جناب " + debtorName + " عزیز،\n" +
                "امیدواریم چرخ کسب‌وکارتان پربرکت بچرخد. 🌹\n\n" +
                "ارادت‌مند شما، حجره " + merchantName + ".\n" +
                "جناب " + debtorName + " بزرگوار، غرض از مزاحمت، جهت یادآوری تراز دفتری فی‌مابین به مبلغ " + 
                String.format("%,d ریال", amount) + " است که " + delayDays + " روز از موعد آن گذشته.\n" +
                "چنانچه برایتان میسر است، دستور فرمایید وجه مذکور تصفیه شود تا در حساب‌رسی صندوق خللی ایجاد نگردد.\n" +
                "حق یارتان، خدا به کار و زندگی‌تان برکت دهد. 🙏";
    }

    /**
     * Launches external application with pre-filled respectful bazaar reminder text.
     */
    public static void shareReminderToPlatform(Context context, Platform platform, String phone, String message) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            String encodedMsg = URLEncoder.encode(message, "UTF-8");
            
            switch (platform) {
                case WHATSAPP:
                    // Formats deep link for WhatsApp
                    intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + phone + "&text=" + encodedMsg));
                    break;

                case TELEGRAM:
                    // Formats deep link for Telegram
                    intent.setData(Uri.parse("tg://msg?text=" + encodedMsg));
                    break;

                case EITAA:
                    // Formats deep link for Eitaa (Iranian messaging app)
                    intent.setData(Uri.parse("https://eitaa.com/share/url?url=" + encodedMsg));
                    break;

                case BALE:
                    // Formats deep link for Bale (Iranian financial/messaging app)
                    intent.setData(Uri.parse("bale://share?text=" + encodedMsg));
                    break;

                case SMS:
                default:
                    // Formats Intent for SMS
                    intent.setAction(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("smsto:" + phone));
                    intent.putExtra("sms_body", message);
                    break;
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        } catch (UnsupportedEncodingException e) {
            Toast.makeText(context, "خطا در پردازش متن ارسالی!", Toast.LENGTH_SHORT).show();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "اپلیکیشن پیام‌رسان مد نظر روی تلفن شما یافت نشد!", Toast.LENGTH_SHORT).show();
        }
    }
}
