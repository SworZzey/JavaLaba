package com.sworzzey.storecatalog.benchmark;

import com.sworzzey.storecatalog.model.Product;
import com.sworzzey.storecatalog.structure.BTreeProductStore;

public class ConcurrentWriteDemo {
    public static void main(String[] args) throws InterruptedException{
        boolean protectedMode = true;

        BTreeProductStore storage = new BTreeProductStore();
        if(protectedMode) {
            System.out.println("PROTECTED STRUCTURE");
        } else {
            System.out.println("UNPROTECTED STRUCTURE");
        }
        Thread thread1 = new Thread(() -> {
            for (int i = 1; i <= 1000; i++) {
                if (protectedMode) {
                    synchronized (storage) {
                        storage.add(new Product(
                                i,
                                "Product " + i,
                                "Benchmark",
                                10.0,
                                100
                        ));
                    }
                } else {
                    storage.add(new Product(
                            i,
                            "Product " + i,
                            "Benchmark",
                            10.0,
                            100
                    ));
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 1001; i <= 2000; i++) {
                if (protectedMode) {
                    synchronized (storage) {
                        storage.add(new Product(
                                i,
                                "Product " + i,
                                "Benchmark",
                                10.0,
                                100
                        ));
                    }
                } else {
                    storage.add(new Product(
                            i,
                            "Product " + i,
                            "Benchmark",
                            10.0,
                            100
                    ));
                }
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();


        System.out.println("Expecting size " + 2000);
        System.out.println("Actual size " + storage.size());


    }
}
