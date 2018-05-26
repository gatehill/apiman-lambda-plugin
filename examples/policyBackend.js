exports.handler = function (request, context, callback) {
    console.log('Request URL: ', request.url);
    console.log('Request method: ', request.httpMethod);
    console.log('Request headers: ', request.headers);
    console.log('Request body: ', request.body);

    // compose the response
    var response = {
        statusCode: 200,
        body: 'Hello world!'
    };
    callback(null, response);
};
