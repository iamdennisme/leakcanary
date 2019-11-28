package com.example.testweakrefrence;


import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

//测试下ReferenceQueue 的逻辑
public class MyClass {
    public static void main(String[] args) {
        Object o = new Object();
        final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
        WeakReference<Object> weakReference = new WeakReference<>(o, referenceQueue);
        System.out.println(weakReference.toString());
        o = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    WeakReference<Object> wr;
                    while ((wr = (WeakReference) referenceQueue.remove()) != null) {
                        System.out.println("回收了:" + wr);
                    }
                } catch (InterruptedException e) {
                    //结束循环
                }
            }
        }).start();
        System.gc();
        System.out.println("finish");


    }

}
