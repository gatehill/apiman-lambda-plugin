exports.handler = function (exchange, context, callback) {
    var request = exchange.request;
    console.log('Request path:', request.path);
    console.log('Request method:', request.httpMethod);
    console.log('Request headers:', request.headers);

    var response = exchange.response;
    console.log('Response status code:', response.statusCode);
    console.log('Response headers:', response.headers);
    console.log('Response body:', response.body);

    // mutate the response
    response.statusCode = 200;
    response.body = 'Hello world!';
    response.headers['X-Custom-Header'] = 'bar';
    callback(null, response);
};
