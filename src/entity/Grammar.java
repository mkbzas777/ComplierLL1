package entity;

import java.util.ArrayList;
import java.util.HashSet;

public class Grammar {

    private String left;
    private String right;
    private ArrayList<ArrayList<String>> content;
    private HashSet<String> first;
    private HashSet<String> follow = new HashSet<>();

    public Grammar() {
    }

    public Grammar(String left, String right, ArrayList<ArrayList<String>> content) {
        this.left = left;
        this.right = right;
        this.content = content;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public ArrayList<ArrayList<String>> getContent() {
        return content;
    }

    public void setContent(ArrayList<ArrayList<String>> content) {
        this.content = content;
    }

    public HashSet<String> getFirst() {
        return first;
    }

    public void setFirst(HashSet<String> first) {
        this.first = first;
    }

    public HashSet<String> getFollow() {
        return follow;
    }

    public void setFollow(HashSet<String> follow) {
        this.follow = follow;
    }

    @Override
    public String toString() {
        return "Grammar{" +
                "left='" + left + '\'' +
                ", right='" + right + '\'' +
                ", content=" + content +
                ", first=" + first +
                ", follow=" + follow +
                '}';
    }
}
