package com.amazon.customskill;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Translator {
 
  private static final String CLIENT_ID = "phenguyen93@gmail.com";
  private static final String CLIENT_SECRET = "b07589a7b3a641d1bcec3edf4381157c";
  private static final String ENDPOINT = "http://api.whatsmate.net/v1/translation/translate";

  /**
   * Entry Point
   */
  public static void main(String[] args) throws Exception {
    // TODO: Specify your translation requirements here:
    String fromLang = "en";
    String toLang = "de";
    String text = "house";

    System.out.println(Translator.translate(fromLang, toLang, text));
  }

 
  public static String translate(String fromLang, String toLang, String text) throws Exception {
    // TODO: Should have used a 3rd party library to make a JSON string from an object
    String jsonPayload = new StringBuilder()
      .append("{")
      .append("\"fromLang\":\"")
      .append(fromLang)
      .append("\",")
      .append("\"toLang\":\"")
      .append(toLang)
      .append("\",")
      .append("\"text\":\"")
      .append(text)
      .append("\"")
      .append("}")
      .toString();

    URL url = new URL(ENDPOINT);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("X-WM-CLIENT-ID", CLIENT_ID);
    conn.setRequestProperty("X-WM-CLIENT-SECRET", CLIENT_SECRET);
    conn.setRequestProperty("Content-Type", "application/json");

    OutputStream os = conn.getOutputStream();
    os.write(jsonPayload.getBytes());
    os.flush();
    os.close();

    int statusCode = conn.getResponseCode();
    System.out.println("Status Code: " + statusCode);
    BufferedReader br = new BufferedReader(new InputStreamReader(
        (statusCode == 200) ? conn.getInputStream() : conn.getErrorStream(),StandardCharsets.UTF_8
      ));
    String output;
    String outputtext="";
    while ((output = br.readLine()) != null) {
    	outputtext+=output;
    }
    
    conn.disconnect();
    return outputtext;
    
    
  }

}