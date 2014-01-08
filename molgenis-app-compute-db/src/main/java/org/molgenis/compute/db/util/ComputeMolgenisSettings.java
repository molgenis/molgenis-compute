package org.molgenis.compute.db.util;

import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.core.RuntimeProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Implementation of MolgenisSettings. Simply returns the default value.
 * 
 * We don't use MolgenisDbSettings so that we don't need a dependency on
 * omx-core
 * 
 * @author erwin
 * 
 */
public class ComputeMolgenisSettings implements MolgenisSettings
{

	private static final Logger logger = Logger.getLogger(ComputeMolgenisSettings.class);

	@Autowired
	@Qualifier("unauthorizedDatabase")
	private DataService database;

	@Override
	public String getProperty(String key)
	{
		return getProperty(key, null);
	}

	@Override
	public String getProperty(String key, String defaultValue)
	{
		QueryRule propertyRule = new QueryRule(RuntimeProperty.IDENTIFIER, Operator.EQUALS,
				RuntimeProperty.class.getSimpleName() + '_' + key);
		List<RuntimeProperty> properties;
		try
		{
			properties = database.find(RuntimeProperty.class, propertyRule);
		}
		catch (DatabaseException e)
		{
			logger.warn(e);
			return defaultValue;
		}
		if (properties == null || properties.isEmpty()) return defaultValue;
		RuntimeProperty property = properties.get(0);
		if (property == null)
		{
			logger.warn(RuntimeProperty.class.getSimpleName() + " '" + key + "' is null");
			return defaultValue;
		}
		return property.getValue();
	}

	@Override
	public void setProperty(String key, String value)
	{

	}

	@Override
	public Boolean getBooleanProperty(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getBooleanProperty(String key, boolean defaultValue)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
