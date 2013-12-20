package org.molgenis.compute.db.plugin;

import org.molgenis.compute.db.controller.EditorController;
import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class EditorPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public EditorPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return EditorController.URI;
	}
}
