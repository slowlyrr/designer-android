package com.bruce.designer.model.result;

import java.io.Serializable;

/**
 * @author liqian
 * 
 */
public class ApiResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private int result;
    
    private int errorcode;

    private String message;

    private Object data;

    public ApiResult() { 
		super();
	}
    
    public ApiResult(int result, Object data, int errorcode, String message) {
		super();
		this.result = result;
		this.errorcode = errorcode;
		this.message = message;
		this.data = data;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public int getErrorcode() {
		return errorcode;
	}

	public void setErrorcode(int errorcode) {
		this.errorcode = errorcode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
    public String toString() {
        return "ApiResult [result=" + result + ",errorcode=" + errorcode + ",message=" + message + ", data=" + data + "]";
    }
}
