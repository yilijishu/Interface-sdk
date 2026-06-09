package com.yilijishu.thirdparty.bean;

import lombok.Data;

@Data
public class ApiRestResult<T> {

    /**
     * 成功状态 200为正常
     */
    private Integer code;
    /**
     * 原始数据
     */
    private String rawData;

    /**
     * 处理后的数据。
     */
    private T data;
}
