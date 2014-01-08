/* Date:        January 6, 2011
 * Template:	PluginScreenJavaTemplateGen.java.ftl
 * generator:   org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.3
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package org.molgenis.compute.db.ui;

import org.molgenis.framework.ui.MolgenisPluginController;

public class ComputeHeader extends MolgenisPluginController
{

	public static final String ID = "computeheader";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static final long serialVersionUID = -6399721492853956608L;

	public ComputeHeader(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}


	@Override
	public String getViewTemplate()
	{
		return "ComputeHeader";
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		return "<link rel=\"stylesheet\" href=\"css/compute.css\" type=\"text/css\">";
	}

}
