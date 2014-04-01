package org.molgenis.compute.db.controller;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.molgenis.compute.db.controller.EditorController.URI;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class EditorController extends MolgenisPluginController
{

	public static final String URI = "/plugin/editor";

	private static final String DEFAULT_APP_EDITOR_HTML = "<p>Editor view</p>";
	private static final String KEY_APP_EDITOR_HTML = "app.editor.html";

	private final MolgenisSettings molgenisSettings;

	@Autowired
	public EditorController(MolgenisSettings molgenisSettings)
	{
		super(URI);
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		String editorHtml = molgenisSettings.getProperty(KEY_APP_EDITOR_HTML, DEFAULT_APP_EDITOR_HTML);
		model.addAttribute(KEY_APP_EDITOR_HTML.replace('.', '_'), editorHtml);

		return "view-editor";
	}

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	@ResponseBody
	public String test()
	{
		String str = "#!/bin/bash\n" +
					 "script to show";
		str = "#!/bin/bash\n" +
				"\n" +
				"# clone the repository\n" +
				"git clone http://github.com/garden/tree\n" +
				"\n" +
				"# generate HTTPS credentials\n" +
				"cd tree\n" +
				"openssl genrsa -aes256 -out https.key 1024\n" +
				"openssl req -new -nodes -key https.key -out https.csr\n" +
				"openssl x509 -req -days 365 -in https.csr -signkey https.key -out https.crt\n" +
				"cp https.key{,.orig}\n" +
				"openssl rsa -in https.key.orig -out https.key\n" +
				"\n" +
				"# start the server in HTTPS mode\n" +
				"cd web\n" +
				"sudo node ../server.js 443 'yes' >> ../node.log &\n" +
				"\n" +
				"# here is how to stop the server\n" +
				"for pid in `ps aux | grep 'node ../server.js' | awk '{print $2}'` ; do\n" +
				"  sudo kill -9 $pid 2> /dev/null\n" +
				"done";

		return str;
	}

	@RequestMapping(value = "/testpost", method = RequestMethod.POST)
	//public String saveWorkflow(@RequestParam("test") String str)
	public String saveWorkflow(@Valid @RequestBody ProtocolUpdate str)
	{
		//str = str.replaceAll("__AND__", "&");
		String toPrint = str.getTest();
		//System.out.println(str);
		System.out.println(toPrint);
		return "view-editor";
	}

	public static class ProtocolUpdate
	{
		@NotNull
		private String test;

		public String getTest()
		{
			return test;
		}

		public void setTest(String test)
		{
			this.test = test;
		}
	}
}
