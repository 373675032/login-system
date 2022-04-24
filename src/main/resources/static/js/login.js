/**
 * 发送短信验证码
 */
function sendSmsCode() {
    let phone = $('#phone').val();
    if (!phone) {
        layer.msg("手机号不能为空");
        return;
    }
    if (!phoneReg(phone)) {
        layer.msg("请输入正确的手机号");
        return;
    }
    $.ajax({
        type: "POST",
        url: "sendSmsCode",
        data: {
            phone: phone,
        },
        dataType: "json",
        success: function (data) {
            if (data['code'] !== 0) {
                layer.msg(data['msg'])
            } else {
                // 禁用按钮，60秒倒计时
                time("#phone-code", 60);
            }
        }
    });
}

/**
 * 发送邮箱验证码
 */
function sendEmailCode() {
    let email = $('#email').val();
    if (!email) {
        layer.msg("邮箱不能为空");
        return;
    }
    if (!emailReg(email)) {
        layer.msg("请输入正确的邮箱地址");
        return;
    }
    $.ajax({
        type: "POST",
        url: "sendEmailCode",
        data: {
            email: email,
        },
        dataType: "json",
        success: function (data) {
            if (data['code'] !== 0) {
                layer.msg(data['msg'])
            } else {
                // 禁用按钮，60秒倒计时
                time("#email-code", 60);
            }
        }
    });
}

/**
 * 60秒倒计时
 * @param o
 */
function time(o, wait) {
    if (wait === 0) {
        $(o).attr("disabled", false);
        $(o).html("重新发送");
    } else {
        $(o).attr("disabled", true);
        $(o).html(wait + "秒后重新发送");
        wait--;
        setTimeout(function () {time(o, wait);},1000);
    }
}