exports.handler = function (request, context, callback) {
    console.log('URL: ' + request.url);
    console.log('Method: ' + request.httpMethod);
    console.log('Headers: ', request.headers);
    console.log('Body: ', request.body);

    var response = {
        statusCode: 200,
        body: 'Hello world!'
    };
    callback(null, response);
};
