/**
 * 
 */
package com.lguipeng.notes.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * the thread pool to execute thread better
 * @author lgp on 2015/1/1
 */
public class ThreadExecutorPool {

    private  ExecutorService pool;

    private final static int DEFAULT_SIZE = 8;

    public ThreadExecutorPool(int size) {
        pool = Executors.newFixedThreadPool(size);
    }

    public ThreadExecutorPool() {
        this(DEFAULT_SIZE);
    }

    /**
     * shutDown the thread pool
     */
    public void shutDown() {
      if (!pool.isShutdown()) {
          pool.shutdown();

          try {
              // Wait a while for existing tasks to terminate
              if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                  pool.shutdownNow(); // Cancel currently executing tasks
               // Wait a while for tasks to respond to being cancelled
              if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                  System.err.println("Pool did not terminate");

           } catch (InterruptedException ie) {
          // (Re-)Cancel if current thread also interrupted
              pool.shutdownNow();
          // Preserve interrupt status
              Thread.currentThread().interrupt();
           }
      }else{
        NotesLog.e("had shut down");
      }
    }

    /**
     * get ExecutorService pool
     * @return ExecutorService pool
     */
    public  ExecutorService getPool() {
      if (pool != null)
        return pool;
      else
        return null;
    }

    /**
     * execute a thread in thread pool
     * @param runnable the thread
     */
    public void execute(Runnable runnable)
    {
      if (!pool.isShutdown())
          pool.execute(runnable);
      else
        NotesLog.e("had shut down");
    }
  
}
