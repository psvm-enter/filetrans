package com.psvm.web.extend;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

/**
 * @author pvsm
 */
public class WebContextListener extends ContextLoaderListener{
    @Override
    public WebApplicationContext initWebApplicationContext(ServletContext servletContext) {
        System.out.println("web容器启动");
        return super.initWebApplicationContext(servletContext);
    }
}
