import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 10);
        Document document = new Document();
        String signature = "your_signature";
        crptApi.createDocument(document, signature);
    }
}