package pers.danisan00.apama;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import com.apama.EngineException;
import com.apama.util.CompoundException;

public class EventEngine {

    private static final Logger logger = Logger.getLogger(EventEngine.class);

    // java EventEngine host port poolsize listenersize mod
    // poolsize refers to how many threads are created as event source
    // listenersize refers to how many msisdn users are located in congestion zone
    // mod refers to how much traffic would have a match
    public static void main(String args[]) {

        System.out.println("Event Engine is started.");

        // initialize parameters
        String host = args[0];
        int port = Integer.valueOf(args[1]);
        int poolsize = Integer.valueOf(args[2]);
        int listenersize = Integer.valueOf(args[3]);
        int mod = Integer.valueOf(args[4]);

        // create new client and inject EPLs as preparation
        ApamaClient mainApamaClient = null;
        try {
            mainApamaClient = new ApamaClient(host, port, "main");
            injectEPLs(mainApamaClient);
        } catch (CompoundException e1) {
            e1.printStackTrace();
        }
        MyLocker ml = new MyLocker(poolsize);
        MyThread[] threads = new MyThread[poolsize];
        for (int i = 0; i < poolsize; i++) {
            threads[i] = new MyThread(host, port, "goodday" + i, ml, listenersize, mod);
            ml.registerListener(threads[i]);
        }

        // create listener thread listens for output event
        ListenerThread listenerThread = new ListenerThread(host, port);
        listenerThread.start();

        try {
            for (int i = 0; i < listenersize; i++) {
                mainApamaClient.sendSensorEvent("ATT" + i, i + 100f);
            }
        } catch (EngineException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        for (int i = 0; i < poolsize; i++) {
            threads[i].start();
        }
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // stop all threads using watcher mechanism
        ml.stopAllThreads();
        listenerThread.stopListener();

        // remove epl from correlator and dump engine info.
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {

            mainApamaClient.dumpEngineStatus();
            removeEPLs(mainApamaClient);
            mainApamaClient.disconnect();

        } catch (CompoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void injectEPLs(ApamaClient apamaClient) {

        ;
        try {
            apamaClient.injectEpl("eventdefinitions/SensorMonitorEvents.mon");
            apamaClient.injectEpl("eventdefinitions/AlertEvents.mon");
            apamaClient.injectEpl("monitors/AlertManager.mon");
            apamaClient.injectEpl("monitors/VSensorMonitor.mon");
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void removeEPLs(ApamaClient apamaClient) {
        try {
            apamaClient.deleteMonitor("pers.danisan00.apama.monitors.SensorMonitor");
            apamaClient.deleteMonitor("pers.danisan00.apama.monitors.AlertManager");
            apamaClient.deleteEventDefinition("pers.danisan00.apama.eventdefinitions.NewSensor");
            apamaClient.deleteEventDefinition("pers.danisan00.apama.eventdefinitions.Temperature");
            apamaClient.deleteEventDefinition("pers.danisan00.apama.eventdefinitions.Pressure");
            apamaClient.deleteEventDefinition("pers.danisan00.apama.eventdefinitions.Alert");
        } catch (Exception e) {

        }
    }

    static class MyThread extends Thread {

        String host = "localhost"; // local Apama Studio 5.0
        String processName = null;
        int port = 15903;

        ApamaClient apamaClient;
        int localCount = 0;
        boolean flag = true;
        MyLocker ml;
        int listenersize = 1;
        int mod = 1;

        public MyThread(String host, int port, String processName, MyLocker ml, int listenersize, int mod) {
            this.host = host;
            this.port = port;
            this.processName = processName;
            this.ml = ml;
            this.listenersize = listenersize;
            this.mod = mod;

            try {
                apamaClient = new ApamaClient(host, port, processName);
            } catch (CompoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public void stopSending() {
            flag = false;
        }

        @Override
        public void run() {
            try {
                while (flag) {
                    int rest = localCount % mod;
                    if (rest == 0) {
                        int tag = (new Random()).nextInt(listenersize);
                        apamaClient.sendTemperatureEvents("ATT" + tag, 42.0f);
                    } else {
                        apamaClient.sendTemperatureEvents("Sprint" + localCount, 42.0f);
                    }
                    localCount++;
                }
                ml.increaseCount(localCount);
                apamaClient.disconnect();
            } catch (EngineException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (CompoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    static class MyLocker {
        int eventCount = 0;
        int closeThreadCount = 0;
        ReadWriteLock rwl = new ReentrantReadWriteLock();
        boolean zwitch = true;
        int poolsize = 0;

        public MyLocker(int poolsize) {
            this.poolsize = poolsize;
            logger.info("MyLocker is created with poolsize=" + poolsize);
        }

        List<MyThread> listListener = new ArrayList<MyThread>();

        public void increaseCount(int adder) {
            rwl.writeLock().lock();
            this.eventCount = this.eventCount + adder;
            this.closeThreadCount++;
            if (this.poolsize == this.closeThreadCount)
                logger.info("finnaly closed, and count is:" + eventCount);
            rwl.writeLock().unlock();
        }

        public void registerListener(MyThread thread) {
            listListener.add(thread);
        }

        public void stopAllThreads() {
            for (int i = 0; i < listListener.size(); i++) {
                listListener.get(i).stopSending();
            }

            logger.info("All sending threads are stoped");
        }

    }

    static class ListenerThread extends Thread {

        String host = "localhost"; // local Apama Studio 5.0
        String processName = null;
        int port = 15903;

        ApamaClient listenerClient;

        public ListenerThread(String host, int port) {
            this.host = host;
            this.port = port;
            try {
                listenerClient = new ApamaClient(host, port, "listener");
            } catch (CompoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                listenerClient.startListeningForOutputAlertEvents();
            } catch (EngineException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public void stopListener() {
            try {
                listenerClient.stopListeningForOutputAlertEvents();
                listenerClient.disconnect();
            } catch (EngineException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (CompoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
