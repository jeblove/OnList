package com.jeblove.onlyList.common;

/**
 * @author : Jeb
 * @date :2023/5/12 23:41
 * @classname :  ResultMsgEnum
 * @description : TODO
 */
public enum ResultMsgEnum {
    SUCCESS(200, "成功"),
    FAIL(500, "失败"),
    AUTH_ERROR(502, "授权失败!"),
    SERVER_BUSY(503, "服务器正忙，请稍后再试!"),
    DATABASE_OPERATION_FAILED(504, "数据库操作失败");
    private int code;
    private String message;

    ResultMsgEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}