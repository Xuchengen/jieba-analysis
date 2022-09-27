package com.github.xuchengen.jieba;

/**
 * <p>结巴运行时异常
 * <p>作者：徐承恩
 * <p>邮箱：<a href="mailto:xuchengen@gmail.com">xuchengen@gmail.com</a>
 * <p>日期：2022-09-27 13:50
 **/
public class JiebaException extends RuntimeException {

    private static final long serialVersionUID = 4694041724566892924L;

    public JiebaException() {
        super();
    }

    public JiebaException(String message) {
        super(message);
    }

    public JiebaException(String message, Throwable cause) {
        super(message, cause);
    }

    public JiebaException(Throwable cause) {
        super(cause);
    }

    protected JiebaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
