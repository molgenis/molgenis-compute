package org.molgenis.compute.db;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.controller.filter.AuthenticationFilter;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class WebAppInitializer implements WebApplicationInitializer
{
	private static final Logger logger = Logger.getLogger(WebAppInitializer.class);

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException
	{
		AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
		ctx.register(WebAppConfig.class);

		// spring
		Dynamic dispatcherServlet = servletContext.addServlet("dispatcher", new DispatcherServlet(ctx));
		if (dispatcherServlet == null)
		{
			logger.warn("ServletContext already contains a complete ServletRegistration for servlet 'dispatcher'");
		}
		else
		{
			dispatcherServlet.setLoadOnStartup(1);
			dispatcherServlet.addMapping("/");
		}

		// enable use of request scoped beans in FrontController
		servletContext.addListener(new RequestContextListener());

		servletContext.addFilter("securityFilter", AuthenticationFilter.class).
				addMappingForServletNames(null, false, "dispatcher");

	}
}