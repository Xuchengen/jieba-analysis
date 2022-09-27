package com.github.xuchengen.jieba;

/**
 * <p>分词令牌
 * <p>作者：徐承恩
 * <p>邮箱：<a href="mailto:xuchengen@gmail.com">xuchengen@gmail.com</a>
 * <p>日期：2022-09-27 12:24
 **/
public class SegToken {

    /**
     * 词
     */
    public String word;

    /**
     * 开始偏移
     */
    public int startOffset;

    /**
     * 结束偏移
     */
    public int endOffset;

    /**
     * 构造方法
     *
     * @param word        词
     * @param startOffset 开始偏移
     * @param endOffset   结束偏移
     */
    public SegToken(String word, int startOffset, int endOffset) {
        this.word = word;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }


    @Override
    public String toString() {
        return "[" + word + ", " + startOffset + ", " + endOffset + "]";
    }

}
