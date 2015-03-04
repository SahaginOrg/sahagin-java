package org.sahagin.runlib.external.adapter;

public interface ScreenSizeAdapter {

    // returns -1 if fails to get width
    int getScreenWidth();

    // returns -1 if fails to get height
    int getScreenHeight();
}
