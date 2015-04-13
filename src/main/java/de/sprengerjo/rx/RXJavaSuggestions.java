package de.sprengerjo.rx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONTokener;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RXJavaSuggestions extends Application {

    private void init(Stage primaryStage, TextField textBox, ListView<String> resultView, Button btn) {
        Group root = new Group();
        primaryStage.setScene(new Scene(root));
        // create text box for typing in
        textBox.setPromptText("Write here");
        textBox.setStyle("-fx-font-size: 34;");
        //create a console for logging key events
        VBox vb = new VBox(10);
        root.getChildren().add(vb);
        vb.getChildren().addAll(textBox, resultView, btn);
    }

    public List<String> makeHTTPPOSTRequest(String s) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://en.wikipedia.org/w/api.php?action=opensearch&search=" + s);
        try {
            post.setHeader("Content-Type", "application/json");
            HttpResponse response = client.execute(post);

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            String json = reader.readLine();
            JSONTokener tokener = new JSONTokener(json);
            JSONArray finalResult = new JSONArray(tokener);

            if (finalResult != null) {
                return Arrays.asList(finalResult.get(1).toString().split(","));
            }
        } catch (IOException e) {
        }
        return null;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final TextField textBox = new TextField();
        final ListView<String> resultView = new ListView<>(FXCollections.<String>observableArrayList());
        Button btn = new Button();
        btn.setText("World");

        final Observable<TextField> observable = Observable.create(subscriber -> {
                textBox.setOnKeyReleased(event -> subscriber.onNext(textBox));
                btn.setOnAction(event -> {
                    textBox.setText(textBox.getText() + " "  + btn.getText());
                    subscriber.onNext(textBox);
                });
            });

        Observable<List<String>> suggestionObservable = observable.map(tB -> tB.getText())
                .debounce(500, TimeUnit.MILLISECONDS)
                .map(text -> text.replaceAll("\\s+","%20"))
                .map(text -> makeHTTPPOSTRequest(text));

        suggestionObservable.forEach(s -> System.out.println(s));

        suggestionObservable.subscribe(suggestions -> Platform.runLater(() -> {
            resultView.getItems().clear();
            resultView.getItems().addAll(suggestions);
        }));

        init(primaryStage, textBox, resultView, btn);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
