package com.github.xuchengen.jieba;

public class Node {
    public Character value;
    public Node parent;

    public Node(Character value, Node parent) {
        this.value = value;
        this.parent = parent;
    }
}
