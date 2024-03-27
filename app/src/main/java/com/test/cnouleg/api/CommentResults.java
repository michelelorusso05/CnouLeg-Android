package com.test.cnouleg.api;

public class CommentResults {
    private Comment[] comments;
    private int count;

    public CommentResults() {}
    public Comment[] getComments() {
        return comments;
    }

    public void setComments(Comment[] comments) {
        this.comments = comments;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
