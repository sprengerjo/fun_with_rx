package de.sprengerjo.rx;

import javafx.concurrent.Task;
import org.junit.*;
import rx.Observable;
import rx.Observer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created with IntelliJ IDEA.
 * User: cojo
 * Date: 13.04.15
 * Time: 22:10
 * To change this template use File | Settings | File Templates.
 */
public class RXJavaSuggestionsTest {
    final BackEnd backEnd = new BackEnd();

    String[] fruits = {"Apple", "Mango", "Peach", "Banana", "Orange", "Grapes", "Watermelon", "Tomato", "Ananas", "Honeymelon"};
    long start;

    @Before
    public void setUp() throws Exception {
        start = System.currentTimeMillis();

    }

    @After
    public void tearDown() throws Exception {
        System.out.println("time elapsed: " + (System.currentTimeMillis() - start));
    }

    @Test
    public void iterativlyObtainSuggestions() throws Exception {
        Observable<String> observable = Observable.from(fruits);
        Observable<List<String>> listObservable = observable.map(fruit -> backEnd.getSuggestionsSlowly(fruit));
        listObservable.forEach(suggestions -> System.out.println(suggestions));

        listObservable.subscribe(suggestions -> assertThat(suggestions.size(), greaterThanOrEqualTo(1)));
    }

    @Test
    public void concurrentlyObtainSuggestions() throws Exception {
        Observable<String> observable = Observable.from(fruits);
        observable.forEach(fruit -> {
            Observer<List<String>> observer = createSuggestionsObserver();
            backEnd.getSuggestions(observer, fruit);
        });
    }

    private Observer<List<String>> createSuggestionsObserver() {
        return new Observer<List<String>>() {
            private List<String> suggestions;

            public void onCompleted() {
                System.out.println(suggestions);
                assertThat(suggestions.size(), greaterThanOrEqualTo(1));
            }

            public void onError(Throwable e) {
                System.out.println(e);
            }

            public void onNext(List<String> args) {
                this.suggestions = args;
            }
        };
    }

}
