import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
public class CrptApi {
    private final Semaphore semaphore;
    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.semaphore = new Semaphore(requestLimit);
        long period = timeUnit.toMillis(1);
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(period);
                    semaphore.release(requestLimit - semaphore.availablePermits());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    private final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    public void createDocument(Document document, String signature) {
        try {
            semaphore.acquire();
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Signature", signature);
            conn.setDoOutput(true);

            String jsonBody = documentToJson(document);

            try (OutputStream outputStream = conn.getOutputStream()) {
                outputStream.write(jsonBody.getBytes());
                outputStream.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Document created successfully");
            } else {
                System.out.println("Failed to create document: " + responseCode + " " + conn.getResponseMessage());
            }
            conn.disconnect();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }
    private String documentToJson(Document document) {
        Gson gson = new Gson();
        return gson.toJson(document);
    }



    // https://ismp.crpt.ru/api/v3/lk/documents/create

}
