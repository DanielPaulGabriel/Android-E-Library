package mdad.localdata.androide_library;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Locale;

public class BookPlayerService extends Service {
    private static final String CHANNEL_ID = "BookPlayerChannel";
    private String bookContent;

    private TextToSpeech tts;
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_PLAY = "ACTION_PLAY";


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        bookContent = intent.getStringExtra("bookContent");
        String bookTitle = intent.getStringExtra("bookTitle");
        String action = intent.getAction();

        if (ACTION_PAUSE.equals(action)) {
            pauseTTS();
            updateNotification(bookTitle, true); // Update the notification to show the play button
        } else if (ACTION_PLAY.equals(action)) {
            resumeTTS();
            updateNotification(bookTitle, false); // Update the notification to show the pause button
        } else {
            // Default case: Start playing the book
            updateNotification(bookTitle, false); // Show the pause button
            speakText(bookContent); // Start TTS
        }

        return START_NOT_STICKY;
    }

    private void updateNotification(String bookTitle, boolean isPaused) {
        Intent pauseIntent = new Intent(this, BookPlayerService.class);
        pauseIntent.setAction("ACTION_PAUSE");
        PendingIntent pausePendingIntent = PendingIntent.getService(
                this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent playIntent = new Intent(this, BookPlayerService.class);
        playIntent.setAction("ACTION_PLAY");
        PendingIntent playPendingIntent = PendingIntent.getService(
                this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Playing Book")
                .setContentText(bookTitle)
                .setSmallIcon(R.drawable.ic_borrowed_books)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .addAction(
                        isPaused ? R.drawable.ic_play : R.drawable.ic_pause,
                        isPaused ? "Play" : "Pause",
                        isPaused ? playPendingIntent : pausePendingIntent
                );

        Notification notification = notificationBuilder.build();
        startForeground(1, notification);
    }

    private void speakText(String bookContent) {
        if (tts != null && bookContent != null && !bookContent.isEmpty()) {
            tts.speak(bookContent, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // No binding for this service
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Book Player Service Channel",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }
    private void pauseTTS() {
        if (tts.isSpeaking()) {

        }
    }

    private void resumeTTS() {
        if (bookContent != null) {
            tts.speak(bookContent, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
}
