package com.taovip.agent.core;

/**
 * service only start and stop once
 *
 * @author chentao
 */
public abstract class BaseService {

  protected volatile boolean RUNNING = false;

  public synchronized void start() throws Exception {
    if (RUNNING) {
      return;
    }
    RUNNING = true;
    start0();
  }

  public synchronized void stop() {
    RUNNING = false;
    stop0();
  }

  protected abstract void start0() throws Exception;

  protected abstract void stop0();
}
