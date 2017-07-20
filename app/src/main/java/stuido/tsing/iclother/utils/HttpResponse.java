package stuido.tsing.iclother.utils;

/**
 * Created by Endless on 2017/7/19.
 */


public class HttpResponse<T> {
    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    private int status;
    private String msg;
    private T data;
}