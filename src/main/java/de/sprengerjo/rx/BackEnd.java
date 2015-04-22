package de.sprengerjo.rx;

import javafx.scene.control.TextField;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONTokener;
import rx.Observable;
import rx.Observer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: cojo
 * Date: 13.04.15
 * Time: 22:14
 * To change this template use File | Settings | File Templates.
 */
public class BackEnd {
        Observable<String> suggestions = Observable.just("");

    public void getSuggestions(Observer<List<String>> observer, String... s) {
        Observable<String> observable = Observable.from(s);
        observable.map(value -> makeHTTPPOSTRequest(value))
                .subscribe(observer);
    }

    public List<String> getSuggestionsSlowly(String s) {
        return makeHTTPPOSTRequest(s);
    }

    List<String> makeHTTPPOSTRequest(String s) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://en.wikipedia.org/w/api.php?action=opensearch&search=" + s);
            post.setHeader("Content-Type", "application/json");
            HttpResponse response = client.execute(post);

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            String json = reader.readLine();
            JSONTokener tokener = new JSONTokener(json);
            JSONArray finalResult = new JSONArray(tokener);

            /**
             * just w8 a little for reasons...
             */
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            if (finalResult != null) {
                List<String> result = Arrays.asList(finalResult.get(1).toString().split(","));
                return result;
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        return null;
    }

}
