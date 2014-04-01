package org.molgenis.compute.db.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.molgenis.compute.db.controller.ExtraController.URI;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class ExtraController extends MolgenisPluginController
{
	String json = "[ { \n" +
			"\"id\": \"graphnode0\", " +
			"\"name\": \"graphnode0\", " +
			"\"data\": {\"$color\": \"#83548B\",  \"$type\": \"rectangle\",  \"$dim\": 10, \"$height\": 30, \"$width\": 60 }, " +
			"\"adjacencies\": [{\"nodeTo\": \"graphnode1\", \"data\": {\"$color\": \"#557EAA\"}}] " +
			"}, " +
			"{ " +
			"\"id\": \"graphnode1\", " +
			"\"name\": \"graphnode1\", " +
			"\"data\": {\"$color\": \"#909291\",  \"$type\": \"rectangle\",  \"$dim\": 10, \"$height\": 60, \"$width\": 30 }, " +
			"\"adjacencies\": []" +
			"}" +
			"]";

	String tree = "{\"id\": \"Start\",\n" +
			"        \"name\": \"Start\",\n" +
			"        \"data\": {\"$color\": \"#23A4FF\", \"$weight\" : 30, \"$height\" : 50, \"$superdata\": \"superdata of node start\"},\n" +
			"        \"children\": [\n" +
			"        {\n" +
			"            \"id\": \"o1\",\n" +
			"            \"name\": \"operation1\",\n" +
			"            \"data\": {\"$color\": \"#0cff14\", \"$weight\" : 60, \"$height\" : 50, \"$superdata\": \"superdata of node operation1\" },\n" +
			"            \"children\": [{\n" +
			"                \"id\": \"o3\",\n" +
			"                \"name\": \"operation3\",\n" +
			"                \"data\": {\"$color\": \"#ff1425\", \"$superdata\": \"superdata of node operation3\"}}]\n" +
			"        },\n" +
			"        {\n" +
			"        \"id\": \"o2\",\n" +
			"        \"name\": \"operation2\",\n" +
			"        \"data\": {\"$color\": \"#feff43\", \"$weight\" : 30, \"$height\" : 80,\"$superdata\": \"superdata of node operation2\"},\n" +
			"        \"children\": [{\n" +
			"        \"id\": \"o3\",\n" +
			"        \"name\": \"operation3\",\n" +
			"        \"data\": {\"$color\": \"#ff1425\", \"$weight\" : 60, \"$height\" : 60, \"$superdata\": \"superdata of node operation3\"}}]\n" +
			"        }" +
			"        ]" +
			"}";

	String test = "{\n" +
			"    \"firstName\": \"John\"}";

	public static final String URI = "/plugin/extra";

	private static final String DEFAULT_APP_EXTRA_HTML = "<p>Workflow view</p>";
	private static final String KEY_APP_EXTRA_HTML = "app.extra.html";

	private final MolgenisSettings molgenisSettings;

	@Autowired
	public ExtraController(MolgenisSettings molgenisSettings)
	{
		super(URI);
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		String extraHtml = molgenisSettings.getProperty(KEY_APP_EXTRA_HTML, DEFAULT_APP_EXTRA_HTML);
		model.addAttribute(KEY_APP_EXTRA_HTML.replace('.', '_'), extraHtml);
		return "view-extra";
	}

	@RequestMapping(value = "/test", method = RequestMethod.GET, produces = "application/json")//{MediaType.APPLICATION_JSON_VALUE}
	@ResponseBody
	public String test()
	{
		return json;
	}

	@RequestMapping(value = "/tree", method = RequestMethod.GET, produces = "application/json")//{MediaType.APPLICATION_JSON_VALUE}
	@ResponseBody
	public String tree()
	{
		return tree;
	}
}
