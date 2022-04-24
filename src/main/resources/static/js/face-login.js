/*
    人脸登录相关方法
 */

//  定义变量
let streams;
let timers = null;
let send;
let loginUrl = "loginByFace";
let video = document.getElementById('face-video');
let canvas = document.getElementById('face-canvas');
let context = canvas.getContext('2d');

/**
 * 发送视频图片
 */
function sendImg() {
    timers = setInterval(function () {
        if (send) {
            send = false;
            context.drawImage(video, 0, 0, 480, 320);
            let image = canvas.toDataURL('image/png');
            $.ajax({
                type: "POST",
                url: loginUrl,
                data: {
                    data: image,
                },
                dataType: "json",
                success: function (data) {
                    console.log(data);
                    if (data['code'] === 0) {
                        layer.msg("登录成功");
                        setTimeout("new function(){window.location.href= '/success'}",1000);
                    } else {
                        layer.msg(data['msg'])
                        send = true
                    }
                }
            });
        }
    }, 2000);
}

/**
 * 开启摄像头
 */
function openCamera() {
    try {
        if (navigator.mediaDevices.getUserMedia || navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia) {
            // 调用用户媒体设备, 访问摄像头
            getUserMedia({video: {width: 480, height: 320}}, successCallBack, errorCallBack);
        } else {
            alert("启动摄像头失败");
        }
    } catch (err) {
        alert("受限于浏览器安全策略，无法启动摄像头");
    }
}

/**
 * 关闭摄像头
 */
function closeCamera() {
    if (streams !== undefined) {
        streams.stop();
    }
    if (!timers) {
        clearInterval(timers);
    }
}

/**
 * 访问用户媒体设备的兼容方法
 * @param constraints
 * @param success 成功回调
 * @param error 失败回调
 */
function getUserMedia(constraints, success, error) {
    if (navigator.mediaDevices.getUserMedia) {
        // 最新的标准API
        navigator.mediaDevices.getUserMedia(constraints).then(success).catch(error);
    } else if (navigator.webkitGetUserMedia) {
        // webkit核心浏览器
        navigator.webkitGetUserMedia(constraints, success, error)
    } else if (navigator.mozGetUserMedia) {
        // firefox浏览器
        navigator.mozGetUserMedia(constraints, success, error);
    } else if (navigator.getUserMedia) {
        // 旧版API
        navigator.getUserMedia(constraints, success, error);
    }
}

/**
 * 成功回调
 * @param stream
 */
function successCallBack(stream) {
    // 兼容webkit核心浏览器
    let CompatibleURL = window.URL || window.webkitURL;
    // 将视频流设置为video元素的源
    streams = stream.getTracks()[0];
    // video.src = CompatibleURL.createObjectURL(stream);
    video.srcObject = stream;
    video.play();
    send = true;
    sendImg();
}

/**
 * 失败回调
 * @param error
 */
function errorCallBack(error) {
    alert(`访问媒体设备失败: ${error.message}`);
}