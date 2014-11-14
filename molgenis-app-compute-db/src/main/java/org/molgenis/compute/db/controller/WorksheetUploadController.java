package org.molgenis.compute.db.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(WorksheetUploadController.URI)
public class WorksheetUploadController extends MolgenisPluginController
{
	private static final Logger LOG = Logger.getLogger(WorksheetUploadController.class);

	public static final String URI = "/plugin/worksheetupload";

	private final FileStore fileStore;

	@Autowired
	public WorksheetUploadController(FileStore fileStore)
	{
		super(URI);
		this.fileStore = fileStore;
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		return "view-worksheetupload";
	}

	@RequestMapping(value = "/upload", method = POST)
	public String uploadWorksheet(@RequestParam("name") String name, @RequestParam("file") MultipartFile file,
			Model model)
	{
		if (name.indexOf('_') != -1)
		{
			model.addAttribute("errorMessage", "Invalid character in Run name '" + "_'");
		}
		else
		{
			String worksheetName = file.getOriginalFilename();
			String fileStoreFileName = name + '_' + worksheetName;
			File existingFile = fileStore.getFile(fileStoreFileName);
			if (existingFile != null && existingFile.exists())
			{
				model.addAttribute("errorMessage", "Worksheet [" + worksheetName + "] for Run [" + name
						+ "] already exists");
			}
			else
			{
				try
				{
					fileStore.store(file.getInputStream(), fileStoreFileName);
					model.addAttribute("successMessage", "Uploaded Worksheet [" + worksheetName + "] for Run [" + name
							+ "]");
				}
				catch (IOException e)
				{
					LOG.error(e);
					model.addAttribute("errorMessage", "An error occured uploading Worksheet [" + worksheetName
							+ "] for Run [" + name + "]");
				}
			}
		}
		return "view-worksheetupload";
	}
}
