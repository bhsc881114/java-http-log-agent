package com.taovip.agent.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.net.httpserver.HttpHandler;
import com.taovip.agent.core.TailerService;

public abstract class AbstractHandler implements HttpHandler {

  protected static Log logger = LogFactory.getLog(AbstractHandler.class);

  protected TailerService tailService = TailerService.getINSTANCE();
}
