package org.molgenis.compute.db.plugin;

import org.molgenis.compute.db.controller.HomeController;
import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class HomePlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public HomePlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return HomeController.URI;
	}
}
