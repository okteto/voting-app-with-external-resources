const {MongoClient} = require('mongodb');

var mongoHost = process.env.MONGODB_HOST || 'mongodb-serverless.tussl.mongodb.net'
var mongoUrl = `mongodb+srv://${process.env.MONGODB_USERNAME}:${encodeURIComponent(process.env.MONGODB_PASSWORD)}@${mongoHost}/${process.env.MONGODB_DATABASE}?retryWrites=true&w=majority`;
const client = new MongoClient(mongoUrl);
const db = client.db(process.env.MONGODB_DATABASE);

var express = require('express'),
    path = require('path'),
    mongo = require('mongodb').MongoClient,
    cookieParser = require('cookie-parser'),
    bodyParser = require('body-parser'),
    methodOverride = require('method-override'),
    app = express(),
    server = require('http').Server(app),
    io = require('socket.io')(server);

io.set('transports', ['polling']);

var port = process.env.PORT || 4000;


async function getVotes() {
    const result = await db.collection('votes').find().toArray();
    return collectVotesFromResult(result);
}

function collectVotesFromResult(result) {
  var votes = { a: 0, b: 0 };
  result.forEach(function(row) {
    votes[JSON.parse(row.body).vote] += 1;
  });
  return votes;
}

function emitVotes(){
  getVotes()
    .then(votes => {
      io.sockets.emit('scores', JSON.stringify(votes));
    })
    .catch(err => {
      console.error('Error performing query: ' + err);
    })
    .finally(() => {
      setTimeout(function() {
        emitVotes();
      }, 1000);
    })
}

io.sockets.on('connection', function (socket) {
  socket.emit('message', { text : 'Welcome!' });

  socket.on('subscribe', function (data) {
    socket.join(data.channel);
  });
});

mongo.connect(mongoUrl, {
  useUnifiedTopology: true,
  useNewUrlParser: true,
  connectTimeoutMS: 1000,
  socketTimeoutMS: 1000,
}, (err, client) => {
  if (err) {
    console.error(`Error connecting to mongodb`, err);
    return;
  }
  console.log("Connected to mongdb")
  const db = client.db(process.env.MONGODB_DATABASE);
  emitVotes(db);
});

app.use(cookieParser());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(methodOverride('X-HTTP-Method-Override'));
app.use(function(req, res, next) {
  res.header("Access-Control-Allow-Origin", "*");
  res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
  res.header("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE, OPTIONS");
  next();
});

app.use(express.static(__dirname + '/views'));

app.get('/votes', function (req, res) {
  getVotes()
    .then(votes => {
      res.send(JSON.stringify(votes))
    })
    .catch(err => {
      console.error('Error performing query: ' + err);
      res.sendStatus(500);
    })
});

app.get('/', function (req, res) {
  res.sendFile(path.resolve(__dirname + '/views/index.html'));
});

server.listen(port, function () {
  var port = server.address().port;
  console.log('App running on port ' + port);
});