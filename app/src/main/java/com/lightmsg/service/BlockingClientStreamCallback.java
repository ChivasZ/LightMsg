package com.lightmsg.service;

import java.io.File;

/**
 * Created by ZhangQh on 2016/2/29.
 */
public interface BlockingClientStreamCallback {
    void start();
    void update(long percent);
    void finish(int ret, String tid, File nf);
}
