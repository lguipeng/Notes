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
public class ExecutorPool {

  private static ExecutorPool executorPool = null;

  private  ExecutorService pool;

  private final static int DEFAULT_SIZE = 5;
  
  private ExecutorPool(int size)
  {
    pool = Executors.newFixedThreadPool(size);
  }
  
  private ExecutorPool()
  {  
    this(DEFAULT_SIZE);
  }
  
  /**
   * 
   * @param size the max number thread in thread pool 
   * @return ExecutorPool
   */
  public static ExecutorPool getInstance(int size)
  {
    if (executorPool == null)
    {
      executorPool = new ExecutorPool(size);
    }
    return executorPool;
  }
  
  /**
   * the pool size is DEFAULT_SIZE=5
   * @return ExecutorPool
   * @see {@link}getInstance(int size)
   */
  public static ExecutorPool getInstance()
  {
    if (executorPool == null)
    {
      executorPool = new ExecutorPool();
    }
    return executorPool;
  }
  
  /**
   * shutDown the thread pool
   */
  public void shutDown()
  {
    if (!pool.isShutdown())
    {
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
      DebugLog.e("the pool has shutdown");
    }
    executorPool = null;

  }
  
  /**
   * get ExecutorService pool
   * @return ExecutorService pool
   */
  public  ExecutorService getPool()
  {
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
        DebugLog.e("the pool has shutdown");
  }
  
}
