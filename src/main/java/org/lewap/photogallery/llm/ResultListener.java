package org.lewap.photogallery.llm;

public interface ResultListener {
    void onResult(String id, String result);
    void onComplete();
    void onError(Exception e);
}