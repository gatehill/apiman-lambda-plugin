exports.handler = function (request, context, callback) {
    console.log('URL: ' + request.url);
    console.log('Method: ' + request.httpMethod);
    console.log('Headers: ', request.headers);
    console.log('Body: ', request.body);

    request.httpMethod = 'POST';
    request.body = 'Hello world!';
    request.headers['X-Custom-Header'] = 'bar';
    callback(null, request);
};
