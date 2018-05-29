exports.handler = function (request, context, callback) {
    console.log('Request path:', request.path);
    console.log('Request method:', request.httpMethod);
    console.log('Request headers:', request.headers);
    console.log('Request body:', request.body);

    // mutate the request
    request.httpMethod = 'POST';
    request.body = 'Hello world!';
    request.headers['X-Custom-Header'] = 'bar';
    callback(null, request);
};
