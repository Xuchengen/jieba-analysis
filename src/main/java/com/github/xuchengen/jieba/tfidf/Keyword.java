package com.github.xuchengen.jieba.tfidf;

/**
 * <p>关键词
 * <p>作者：徐承恩
 * <p>邮箱：<a href="mailto:xuchengen@gmail.com">xuchengen@gmail.com</a>
 * <p>日期：2022-09-27 14:15
 **/
public class Keyword implements Comparable<Keyword> {
    private Double tfidfvalue;
    private String name;

    public double getTfidfvalue() {
        return tfidfvalue;
    }

    public void setTfidfvalue(double tfidfvalue) {
        this.tfidfvalue = tfidfvalue;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public Keyword(String name, Double tfidfvalue) {
        this.name = name;
        // tfidf值只保留3位小数
        this.tfidfvalue = (double) Math.round(tfidfvalue * 10000) / 10000;
    }

    /**
     * 为了在返回tdidf分析结果时，可以按照值的从大到小顺序返回，故实现Comparable接口
     */
    @Override
    public int compareTo(Keyword o) {
        return o.tfidfvalue.compareTo(this.tfidfvalue);
    }

    /**
     * 重写hashcode方法，计算方式与原生String的方法相同
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        long temp;
        temp = Double.doubleToLongBits(tfidfvalue);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Keyword other = (Keyword) obj;
        if (name == null) {
            return other.name == null;
        } else return name.equals(other.name);
    }

}

