exports.handler = function (event, context, callback) {
    console.log('Method: ' + event.httpMethod);
    console.log('Headers: ', event.headers);
    console.log('Body: ', event.body);

    var response = {
        statusCode: 200,
        body: 'Hello world!'
    };
    callback(null, response);
};
