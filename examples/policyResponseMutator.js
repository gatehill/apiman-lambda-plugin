exports.handler = function (exchange, context, callback) {
    var request = exchange.request;
    console.log('Request URL: ' + request.url);
    console.log('Request method: ' + request.httpMethod);
    console.log('Request headers: ' + request.headers);

    var response = exchange.response;
    console.log('Response status code: ' + response.statusCode);
    console.log('Response headers: ' + response.headers);
    console.log('Response body: ' + response.body);

    response.statusCode = 200;
    response.body = 'Hello world!';
    callback(null, response);
};
