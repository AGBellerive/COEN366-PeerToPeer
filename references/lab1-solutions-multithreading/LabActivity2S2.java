package main;
import java.util.Random;

public class LabActivity2S2 {
    private static final Object lock = new Object();
    private static int randomNumber;
    private static boolean generated = false;

    public static void main(String[] args) {
        // Create multiple number generator threads
        for (int i = 0; i < 3; i++) {
            Thread numberGeneratorThread = new Thread(new NumberGenerator());
            numberGeneratorThread.start();
        }

        // Create multiple number comparator threads
        for (int i = 0; i < 2; i++) {
            Thread numberComparatorThread = new Thread(new NumberComparator());
            numberComparatorThread.start();
        }
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
                        System.out.println(Thread.currentThread().getName() + " Generated Num: " + randomNumber);
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
                            System.out.println(Thread.currentThread().getName() + " - " + randomNumber + " is greater than 10");
                        } else if (randomNumber < 10) {
                            System.out.println(Thread.currentThread().getName() + " - " + randomNumber + " is not greater than 10");
                        } else if (randomNumber == 10) {
                            System.out.println(Thread.currentThread().getName() + " - " + randomNumber + " is equal to 10");
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
