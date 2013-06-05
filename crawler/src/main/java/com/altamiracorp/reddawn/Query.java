package com.altamiracorp.reddawn;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;

/**
 * Hello world!
 *
 */
public class Query
{
    public static void main( String[] args )   throws Exception
    {
        System.out.println( "Hello Sam!" );
        // Testing to make sure we're using git correctly.

        // The request also includes the userip parameter which provides the end
// user's IP address. Doing so will help distinguish this legitimate
// server-side traffic from traffic which doesn't come from an end-user.
        URL url = new URL(
                "https://ajax.googleapis.com/ajax/services/search/web?v=1.0&"
                        + "q=Paris%20Hilton&userip=USERS-IP-ADDRESS");
        URLConnection connection = url.openConnection();
        connection.addRequestProperty("Referer", "nearinfinity.com");

        String line;
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while((line = reader.readLine()) != null) {
            builder.append(line);
        }

        JSONObject json = new JSONObject(builder.toString());
        System.out.println(json.toString());
// now have some fun with the results...

    }
}
