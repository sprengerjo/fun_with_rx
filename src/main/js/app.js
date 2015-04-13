$(function () {
    var $input = $('#input'),
        $results = $('#results');

    function searchWikipedia(term) {
        return $.ajax({
            url: 'http://en.wikipedia.org/w/api.php',
            dataType: 'jsonp',
            data: {
                action: 'opensearch',
                format: 'json',
                search: term
            }
        }).promise();
    }

    /* Only get the value from each key up */
    var keyups = Rx.Observable.fromEvent($input, 'keyup')
        .map(e => e.target.value)
        .filter(text => text.length > 2);
    /* Now debounce the input for 500ms */
    var debounced = keyups.debounce(500 /* ms */);
    /* Now get only distinct values, so we eliminate the arrows and other control characters */
    var distinct = debounced.distinctUntilChanged();

    var suggestions = distinct.flatMapLatest(searchWikipedia);

    suggestions.forEach(
        function (data) {
            $results
                .empty()
                .append(data[1].map(value => $('<li>').text(value)));
        },
        function (error) {
            $results
                .empty()
                .append($('<li>'))
                .text('Error:' + error);
        });

});