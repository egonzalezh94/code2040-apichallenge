import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class testApacheHttpClient {
    private static Logger logger = LogManager.getLogger();
    private final CloseableHttpClient client;
    private final HttpPost post;
    private final HttpPost validatePost;
    private final String jsonString = "{\"token\":\"uYBuUp3d6Z\"}";

    public testApacheHttpClient(String uri, String validateUri) {

        client = HttpClients.createDefault();
        post = new HttpPost(uri);
        validatePost = new HttpPost(validateUri);

    }

    public void doPost() {
        StringEntity se;
        HttpResponse response;
        try {
            se = new StringEntity(jsonString);
            post.setEntity(se);
            response = client.execute(post);
            InputStream stream = response.getEntity().getContent();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    stream));
            JSONParser parser = new JSONParser(); // Will parse the String
                                                  // converted from the
                                                  // InputStream
            String line = null;
            String revWord = null;
            while ((line = reader.readLine()) != null) { // Go through stream
                                                         // until null

                JSONObject jsonObject = (JSONObject) parser.parse(line); // Parses
                                                                         // each
                                                                         // line
                                                                         // and
                                                                         // converts
                                                                         // it
                                                                         // into
                                                                         // JSONObject

                if (jsonObject.containsKey("result")) {
                    logger.debug("Word needed to reverse: "
                            + jsonObject.get("result"));
                    revWord = reverseString((String) jsonObject.get("result"));
                    validateResult("uYBuUp3d6Z", revWord);
                }
            }

            // Send result as JSON
            logger.debug("Reversed word:" + revWord);

        } catch (UnsupportedEncodingException e) {
            logger.debug(e);
        } catch (ClientProtocolException e) {
            logger.debug(e);
        } catch (IOException e) {
            logger.debug(e);
        } catch (ParseException e) {
            logger.debug(e);
        }

    }

    public String reverseString(String word) {
        StringBuilder revWord = new StringBuilder(word.length());
        for (int i = word.length() - 1; i >= 0; i--) {
            revWord.append(word.charAt(i));
        }

        return revWord.toString();

    }

    public void validateResult(String token, String result) {
        URI myuri;
        StringEntity entity;
        HttpResponse response;
        try {
            String jsonText = String.format(
                    "{\"token\":\"%s\", \"string\": \"%s\"}", token, result);
            logger.debug(jsonText);
            entity = new StringEntity(jsonText);
            validatePost.setEntity(entity);
            response = client.execute(validatePost);
            InputStream stream = response.getEntity().getContent();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    stream));
            JSONParser parser = new JSONParser(); // Will parse the String
                                                  // converted from the
                                                  // InputStream
            String line = null;
            String revWord = null;

            while ((line = reader.readLine()) != null) { // Go through stream
                                                         // until null

                logger.debug("result after validation" + line);

            }

        } catch (UnsupportedEncodingException e) {
            logger.debug(e);
        } catch (ClientProtocolException e) {
            logger.debug(e);
        } catch (IOException e) {
            logger.debug(e);
        }

    }

    public static void main(String[] args) {
        testApacheHttpClient test = new testApacheHttpClient(
                "http://challenge.code2040.org/api/getstring",
                "http://challenge.code2040.org/api/validatestring");
        test.doPost();
    }

}
