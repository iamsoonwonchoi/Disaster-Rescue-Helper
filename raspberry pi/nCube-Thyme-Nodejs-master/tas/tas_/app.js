/**
 * Created by ryeubi on 2015-08-31.
 * Updated 2017.03.06
 * Made compatible with Thyme v1.7.2
 */

var net = require('net');
var util = require('util');
var fs = require('fs');
var xml2js = require('xml2js');


var wdt = require('./wdt');
//var sh_serial = require('./serial');

var serialport = require('serialport');
var Serial = serialport.SerialPort,
    portName = '/dev/ttyACM0',
    sp = new Serial(portName),
    val = 0;

var usecomport = '';
var usebaudrate = '';
var useparentport = '';
var useparenthostname = '';
var time_go = 0;
var direction = 0;

var upload_arr = [];
var download_arr = [];

var conf = {};



sp.on('open',function(){
    console.log('ON');
    sp.on('data',function(data){
        // console.log("time :  ", data[0]);
        // console.log("direction : ", data[1]);
        // time_go = data[0];
        // direction = data[1];
        direction = data[0]
    });
});     

// This is an async file read
fs.readFile('conf.xml', 'utf-8', function (err, data) {
    if (err) {
        console.log("FATAL An error occurred trying to read in the file: " + err);
        console.log("error : set to default for configuration")
    }
    else {
        var parser = new xml2js.Parser({explicitArray: false});
        parser.parseString(data, function (err, result) {
            if (err) {
                console.log("Parsing An error occurred trying to read in the file: " + err);
                console.log("error : set to default for configuration")
            }
            else {
                var jsonString = JSON.stringify(result);
                conf = JSON.parse(jsonString)['m2m:conf'];

                usecomport = conf.tas.comport;
                usebaudrate = conf.tas.baudrate;
                useparenthostname = conf.tas.parenthostname;
                useparentport = conf.tas.parentport;

                if(conf.upload != null) {
                    if (conf.upload['ctname'] != null) {
                        upload_arr[0] = conf.upload;
                    }
                    else {
                        upload_arr = conf.upload;
                    }
                }

                if(conf.download != null) {
                    if (conf.download['ctname'] != null) {
                        download_arr[0] = conf.download;
                    }
                    else {
                        download_arr = conf.download;
                    }
                }
            }
        });
    }
});

/////////////////////////////////////////////////////////
// homework_1.js (수신)
// var SerialPort = require('serialport'),
//     portName = '/dev/ttyACM0',
//     sp = new SerialPort(portName),
//     val = 0;

// sp.on('open',function(){
//     console.log('ON');
//     sp.on('data',function(data){
//         console.log("time :  ", data[0]);
//         console.log("direction : ", data[1]);
//         time_go = data[0];
//         direction = data[1];
//     });
// });     

////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////
// homework_1.js (송신)
// var server_r = require('net').createServer();
// var socket_r;

// var send_data=0;

// server_r.on('connection', function(socket_r){
// 	console.log('New connection_r');

// 	setInterval(function(){
// 		try{
//             socket_r.write(send_data);
// 		}
// 		catch(e){}
//     }, 1000);
// });

// server_r.on('error', function(err){
// 	console.log('Server Error_r:', err.message);
// });

// server_r.on('close', function(){
// 	console.log('Server Closed_r');
// });

// server_r.listen(4000);
////////////////////////////////////////////////////////////



var tas_state = 'init';

var upload_client = null;

var t_count = 0;

// sensor로부터 받아온 값이 getinfo이다.
// thyme_tas.js의 tas_handler로 들어가게 된다.
function timer_upload_action() {
    // if (tas_state == 'upload') {
    //     var con = {value: 'TAS' + t_count++ + ',' + '55.2'};
    //     for (var i = 0; i < upload_arr.length; i++) {
    //         if (upload_arr[i].id == 'timer') {
    //             var cin = {ctname: upload_arr[i].ctname, con: con};
    //             console.log(JSON.stringify(cin) + ' ---->');
    //             upload_client.write(JSON.stringify(cin) + '<EOF>');
    //             break;
    //         }
    //     }
    // }
    if(tas_state=='upload'){
        // var a = time_go*1000+direction;
        var a = direction;

        var con = 99;
        console.log(a);
        if (isNaN(a)){
            console.log(a)
            con = 99;
        }else {
            con = a;
        }
        // console.log("확인!!!!" + c);
        // console.log("확인!!!!" + sensor_value);
        // var con = 'getinfo';
        for(var i = 0; i < upload_arr.length; i++){
            var cin = {ctname: upload_arr[i].ctname, con:con };
            console.log(JSON.stringify(cin) + '---->');
            // 직렬화
            // socket write
            // Thyme으로 string 형태로 전송한다.
            upload_client.write(JSON.stringify(cin)+'<EOF>'); // EOF는 나중에 EOF단위로 편하게 파싱하게 해주기 위해서 붙인 것이다.
            // console.log("(1) TAS -> Thyme 메세지 전송 :" + con);
            break;
        }

    }
}

function serial_upload_action() {
    if (tas_state == 'upload') {
        var buf = new Buffer(4);
        buf[0] = 0x11;
        buf[1] = 0x01;
        buf[2] = 0x01;
        buf[3] = 0xED;
        myPort.write(buf);
    }
}

var tas_download_count = 0;

function on_receive(data) {
    if (tas_state == 'connect' || tas_state == 'reconnect' || tas_state == 'upload') {
        var data_arr = data.toString().split('<EOF>');
        if(data_arr.length >= 2) {
            for (var i = 0; i < data_arr.length - 1; i++) {
                var line = data_arr[i];
                var sink_str = util.format('%s', line.toString());
                var sink_obj = JSON.parse(sink_str);

                send_data = sink_obj['name'];

                // console.log('수정 부분1 : ' + line);
                // console.log('수정 부분2 : ' + sink_str);
                // console.log('수정 부분3 : ' + sink_obj);
                console.log('(7) TAS의 수신부분 : ' + sink_obj['name']);

                if (sink_obj.ctname == null || sink_obj.con == null) {
                    console.log('Received: data format mismatch');
                }
                else {
                    if (sink_obj.con == 'hello') {
                        console.log('Received: ' + line);

                        if (++tas_download_count >= download_arr.length) {
                            tas_state = 'upload';
                        }
                    }
                    else {
                        for (var j = 0; j < upload_arr.length; j++) {
                            if (upload_arr[j].ctname == sink_obj.ctname) {
                                console.log('ACK : ' + line + ' <----');
                                break;
                            }
                        }

                        for (j = 0; j < download_arr.length; j++) {
                            if (download_arr[j].ctname == sink_obj.ctname) {
                                g_down_buf = JSON.stringify({id: download_arr[i].id, con: sink_obj.con});
                                console.log(g_down_buf + ' <----');
                                myPort.write(g_down_buf);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}


//var SerialPort = null;
var myPort = null;
function tas_watchdog() {
    if(tas_state == 'init') {
        upload_client = new net.Socket();

        upload_client.on('data', on_receive);

        upload_client.on('error', function(err) {
            console.log(err);
            tas_state = 'reconnect';
        });

        upload_client.on('close', function() {
            console.log('Connection closed');
            upload_client.destroy();
            tas_state = 'reconnect';
        });

        if(upload_client) {
            console.log('tas init ok');
            tas_state = 'init_serial';
        }
    }
    else if(tas_state == 'init_serial') {
    	SerialPort = serialport.SerialPort;
    	
        serialport.list(function (err, ports) {
            ports.forEach(function (port) {
                console.log(port.comName);
            });
        });

        myPort = new SerialPort(usecomport, {
            baudRate : parseInt(usebaudrate, 10),
            buffersize : 1
            //parser : serialport.parsers.readline("\r\n")
        });

        myPort.on('open', showPortOpen);
        myPort.on('data', saveLastestData);
        myPort.on('close', showPortClose);
        myPort.on('error', showError);

        if(myPort) {
            console.log('tas init serial ok');
            tas_state = 'connect';
        }
    }
    else if(tas_state == 'connect' || tas_state == 'reconnect') {
        upload_client.connect(useparentport, useparenthostname, function() {
            console.log('upload Connected');
            tas_download_count = 0;
            for (var i = 0; i < download_arr.length; i++) {
                console.log('download Connected - ' + download_arr[i].ctname + ' hello');
                var cin = {ctname: download_arr[i].ctname, con: 'hello'};
                upload_client.write(JSON.stringify(cin) + '<EOF>');
            }

            if (tas_download_count >= download_arr.length) {
                tas_state = 'upload';
            }
        });
    }
}

wdt.set_wdt(require('shortid').generate(), 1, timer_upload_action);
wdt.set_wdt(require('shortid').generate(), 3, tas_watchdog);
//wdt.set_wdt(require('shortid').generate(), 3, serial_upload_action);

var cur_c = '';
var pre_c = '';
var g_sink_buf = '';
var g_sink_ready = [];
var g_sink_buf_start = 0;
var g_sink_buf_index = 0;
var g_down_buf = '';

function showPortOpen() {
    console.log('port open. Data rate: ' + myPort.options.baudRate);
}

var count = 0;
function saveLastestData(data) {
    var val = data.readUInt16LE(0, true);

    if(g_sink_buf_start == 0) {
        if(val == 0x16) {
            count = 1;
            g_sink_buf_start = 1;
            g_sink_ready.push(val);
        }
    }
    else if(g_sink_buf_start == 1) {
        if(val == 0x05) {
            count = 2;
            g_sink_buf_start = 2;
            g_sink_ready.push(val);
        }
    }
    else if(g_sink_buf_start == 2) {
        if(val == 0x01) {
            count = 3;
            g_sink_buf_start = 3;
            g_sink_ready.push(val);
        }
    }
    else if(g_sink_buf_start == 3) {
        count++;
        g_sink_ready.push(val);

        if(count >= 9){
            console.log(g_sink_ready);

            /*CO2 통신 예제
            SEND(4바이트) : 0x11, 0x01, 0x01, 0xED
            Respond(8바이트) : 0x16, 0x05, 0x01, 0x02, 0x72, 0x01, 0xD6, 0x99
            응답의 0x16, 0x05, 0x01 은 항상 같은 값을 가지며, 빨간색 글씨의 0x02, 0x72 가 농도를 나타내는 수치입니다.
            (HEX) 0x0272 = 626
            즉, 농도는 626 ppm 입니다. */

            var nValue = g_sink_ready[3] * 256 + g_sink_ready[4];

            console.log(nValue);

            if(tas_state == 'upload') {
                for(var i = 0; i < upload_arr.length; i++) {
                    if(upload_arr[i].ctname == 'co2') { // container name을 co2로 바꿨기때문에 co2로 수정
                        var cin = {ctname: upload_arr[i].ctname, con: nValue.toString()};
                        console.log('SEND : ' + JSON.stringify(cin) + ' ---->');
                        upload_client.write(JSON.stringify(cin) + '<EOF>');
                        break;
                    }
                }
            }

            g_sink_ready = [];
            count = 0;
            g_sink_buf_start = 0;
        }
    }
}

function showPortClose() {
    console.log('port closed.');
}

function showError(error) {
    var error_str = util.format("%s", error);
    console.log(error.message);
    if (error_str.substring(0, 14) == "Error: Opening") {

    }
    else {
        console.log('SerialPort port error : ' + error);
    }
}

