package org.molgenis.compute.db.plugin;

import org.molgenis.compute.db.controller.ExtraController;
import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class ExtraPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public ExtraPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return ExtraController.URI;
	}
}
