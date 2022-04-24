package world.xuewei.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 相应数据封装
 * @date 上午10:52 2022/4/19
 * @author XUEW
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseResult {
    private int code;
    private String msg;
    private Object data;

    public static ResponseResult success() {
        return success(null);
    }

    public static ResponseResult success(Object data) {
        return new ResponseResult(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMsg(), data);
    }

    public static ResponseResult failure(ResponseCode errorCode) {
        return failure(errorCode.getCode(), errorCode.getMsg());
    }

    public static ResponseResult failure(int code, String msg) {
        return new ResponseResult(code, msg, null);
    }

    public static ResponseResult failure(ResponseCode errorCode, Object data) {
        return new ResponseResult(errorCode.getCode(), errorCode.getMsg(), data);
    }

    public static ResponseResult failure(ResponseCode errorCode, Object[] params) {
        return failure(errorCode, params, null);
    }

    public static ResponseResult failure(ResponseCode errorCode, Object[] params, Object data) {
        String msg = String.format(errorCode.getMsg(), params);
        return new ResponseResult(errorCode.getCode(), msg, data);
    }
}
