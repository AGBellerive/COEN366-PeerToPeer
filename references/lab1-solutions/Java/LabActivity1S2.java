package main;

import java.util.Random;

public class LabActivity1S2 {
    private static final Object lock = new Object();
    private static int randomNumber;
    private static boolean generated = false;

    public static void main(String[] args) {
        Thread numberGeneratorThread = new Thread(new NumberGenerator());
        Thread numberComparatorThread = new Thread(new NumberComparator());

        numberGeneratorThread.start();
        numberComparatorThread.start();
    }

    static class NumberGenerator implements Runnable {
        private final Random random = new Random();

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000); // Generate a number every 1 second
                    synchronized (lock) {
                        randomNumber = random.nextInt(20); // Generate a random number between 0 and 19
                        System.out.println(" Generated Num: "+randomNumber );

                        generated = true;
                        lock.notify();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class NumberComparator implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (lock) {
                        while (!generated) {
                            lock.wait();
                        }
                        if (randomNumber > 10) {
                            System.out.println(randomNumber + " is greater than 10");
                        } else if (randomNumber < 10) { 
                            System.out.println(randomNumber + " is not greater than 10");
                        }
                        else if (randomNumber == 10) { 
                            System.out.println(randomNumber + " is equal to 10");
                        }
                        generated = false;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
