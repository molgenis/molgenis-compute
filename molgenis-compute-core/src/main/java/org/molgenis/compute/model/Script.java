package org.molgenis.compute.model;

import freemarker.template.Template;

public class Script
{
	private Template headerTemplate;
	private Template footerTemplate;
	private Template submitTemplate;

	public Script(Template headerTemplate, Template footerTemplate)
	{
		this.headerTemplate = headerTemplate;
		this.footerTemplate = footerTemplate;
	}

	public Template getHeaderTemplate()
	{
		return headerTemplate;
	}

	public Template getFooterTemplate()
	{
		return footerTemplate;
	}

	public Template getSubmitTemplate()
	{
		return submitTemplate;
	}

	public void setSubmitTemplate(Template submitTemplate)
	{
		this.submitTemplate = submitTemplate;
	}
}
