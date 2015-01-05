import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class testApacheHttpClient {
    private static Logger logger = LogManager.getLogger();
    private final CloseableHttpClient client;
    private final HttpPost post;
    private final HttpPost validatePost;
    private final String jsonString = "{\"token\":\"uYBuUp3d6Z\"}";
    private final String action;

    public testApacheHttpClient(String uri, String validateUri, String action) {

        client = HttpClients.createDefault();
        post = new HttpPost(uri);
        validatePost = new HttpPost(validateUri);
        this.action = action;

    }

    @SuppressWarnings("unchecked")
    // TODO: Check the warnings and way to avoid this.
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
            //TODO: Localize all the instances of variables made.
            while ((line = reader.readLine()) != null) { // Go through stream
                                                         // until null

                JSONObject jsonObject = (JSONObject) parser.parse(line); // Parses each line and convert it into JSONObject
                logger.debug(jsonObject);
                if (jsonObject.containsKey("result")) {
                    logger.debug("Word needed to reverse: "
                            + jsonObject.get("result"));
                    switch (action) {
                        case "getsring":
                            revWord = reverseString((String) jsonObject.get("result"));
                            validateResult("uYBuUp3d6Z", revWord);
                            break;
                        case "haystack":
                            int match = -1;
                            String haystackDict = jsonObject.get("result").toString();
                            JSONObject haystackObject = (JSONObject) parser.parse(haystackDict);
                            //JSONArray arr = (JSONArray) haystackDict;
                            logger.debug(haystackObject.getClass());
                            String needle = (String) haystackObject.get("needle");
                            JSONArray haystack = (JSONArray) haystackObject.get("haystack");
                            logger.debug("needle: " + needle);
                            logger.debug("haystack: " + haystack.toString());
                            for (int i=0; i<haystack.size(); i++){
                                if (haystack.get(i).toString().equals(needle)) {
                                    match = i;
                                }
                                logger.debug(haystack.get(i).toString());
                            }
                            if (match != -1) {
                                validateResult("uYBuUp3d6Z", String.valueOf(match));
                            }
//                            Map<String,String> mapHaystack = (Map<String, String>) haystackDict;
//                            logger.debug(mapHaystack.get("haystack").getClass());
                            
                            break;
                        case "prefix":
                            JSONArray newJsonArr = new JSONArray();
                            String prefixDict = jsonObject.get("result").toString();
                            JSONObject prefixObject = (JSONObject) parser.parse(prefixDict);
                            //JSONArray arr = (JSONArray) haystackDict;
                            logger.debug(prefixObject.getClass());
                            String prefix = (String) prefixObject.get("prefix");
                            JSONArray wordArray = (JSONArray) prefixObject.get("array");
                            ArrayList<String> newArray = new ArrayList<String>();
                            logger.debug("prefix: " + prefix);
                            logger.debug("array: " + wordArray.toString());
                            for (int i=0; i<wordArray.size(); i++){
                                logger.debug(wordArray.get(i).toString());
                                if (!wordArray.get(i).toString().startsWith(prefix)) {
                                    newArray.add(wordArray.get(i).toString());
                                }

                            }
                            newJsonArr.addAll(newArray);
                            
                            validateResult("uYBuUp3d6Z", newJsonArr);

                            
                            break;
                            
                        case "time":
                            String timeDict = jsonObject.get("result").toString();
                            JSONObject timeObject = (JSONObject) parser.parse(timeDict);
                            String time = timeObject.get("datestamp").toString();
                            logger.debug(time);
                            String interval = timeObject.get("interval").toString();
                            // http://stackoverflow.com/questions/19714018/iso-8601-date-format-to-unix-time-in-java
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            logger.debug(df.getTimeZone());
                            df.setTimeZone(TimeZone.getTimeZone("GMT"));
                            logger.debug(df.getTimeZone());
                            //TODO: Delete try catch and add exceptions to big try
                            try {
                                long date = df.parse(time).getTime()/1000;
                                String datetime = df.parse(time).toString();
                                logger.debug(df.getTimeZone());
                                logger.debug(datetime);
                                logger.debug("old date:" + date);
                                date = date + Integer.parseInt(interval);
                                logger.debug(date);
                                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                df2.setTimeZone(TimeZone.getTimeZone("GMT"));
                                logger.debug(new Date(date));
                                String finalTime = df2.format(new Date(date*1000));
                                logger.debug(finalTime);
                                validateResult("uYBuUp3d6Z", finalTime);
                            } catch (java.text.ParseException e) {
                                logger.debug(e);
                            }

                            
                            
                            logger.debug(timeDict);
                            break;
                    }
                }
            }

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
        StringEntity entity;
        HttpResponse response;
        try {
            //TODO: Switch case to decide the name of dictionary key
            String jsonText = String.format(
                    "{\"token\":\"%s\", \"datestamp\": \"%s\"}", token, result); //Changed string to datestamp for ex 4
            logger.debug(jsonText);
            entity = new StringEntity(jsonText);
            validatePost.setEntity(entity);
            response = client.execute(validatePost);
            InputStream stream = response.getEntity().getContent();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    stream));
            String line = null;

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
    
    public void validateResult(String token, JSONArray result) {
        StringEntity entity;
        HttpResponse response;
        try {
            //TODO: Switch case to decide the name of dictionary key
            String jsonText = String.format(
                    "{\"token\":\"%s\", \"array\": %s}", token, result); //Changed string to array for ex 3
                                                                         //NOTE: format string doesnt contain quotes for sending jsonarray value
            logger.debug(jsonText);
            entity = new StringEntity(jsonText);
            validatePost.setEntity(entity);
            response = client.execute(validatePost);
            InputStream stream = response.getEntity().getContent();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    stream));
            String line = null;

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
                "http://challenge.code2040.org/api/time",
                "http://challenge.code2040.org/api/validatetime", "time");
        test.doPost();
    }

}
