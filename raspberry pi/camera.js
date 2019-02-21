var express = require('express'),
    http = require('http'),
    app = express(),
    server = http.createServer(app);

app.use(express.static(__dirname + '/home/pi/Pictures')) ;

var img_flag = 0 ;

var cameraOptions = {
  width : 600,
  height : 420,
  mode : 'timelapse',
  awb : 'off',
  encoding : 'jpg',
  output : '/home/pi/Pictures/camera.jpg',
  q : 50,
  timeout : 10000,
  timelapse : 0,
  nopreview : true,
  th : '0:0:0'
};

var camera = new require('raspicam')(cameraOptions) ;

camera.start() ;

camera.on('exit', function() {
    camera.stop() ;
    console.log('Restart camera') ;
    camera.start() ;
  }) ;

camera.on('read', function() {
    // img_flag = 1 ;
    console.log('1');
  }) ;

app.get('/cam', function(req, res) {
    // 기본 코드
    res.sendfile('cam.html', {root : __dirname});

    // // 딜레이 추가 코드
    // setTimeout(function(){
    //   res.sendfile('cam.html', {root : __dirname});
    // }, 1000);
    console.log('2');
  }) ;

app.get('/img', function (req, res) {
    // // 딜레이 추가 코드
    // setTimeout(function(){
    //   console.log('get /img');
    //   if(img_flag == 1){
    //     img_flag = 0;
    //     res.sendfile('/home/pi/Pictures/camera.jpg');
    //   }
    // }, 1000);

    // // 기본 코드
    // console.log('get /img');
    // if (img_flag == 1) {
    //   img_flag = 0 ;
    //   res.sendfile('/home/pi/Pictures/camera.jpg') ;
    // }

    // ima_flag 제거 코드
    console.log('get /img');
    res.sendfile('/home/pi/Pictures/camera.jpg');
    console.log('3');
});

server.listen(8000, function() {
    console.log('express server listening on port ' + server.address().port) ;
  }) ;