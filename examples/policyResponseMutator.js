exports.handler = function (response, context, callback) {
    console.log('Headers: ', response.headers);
    console.log('Body: ', response.body);

    response.statusCode = 200;
    response.body = 'Hello world!';
    callback(null, response);
};
