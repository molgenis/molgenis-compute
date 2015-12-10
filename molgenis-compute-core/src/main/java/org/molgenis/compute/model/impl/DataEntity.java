package org.molgenis.compute.model.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.AbstractEntity;
import org.molgenis.util.CaseInsensitiveLinkedHashMap;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.gs.collections.impl.map.mutable.UnifiedMap;

/**
 * Simple Entity implementation based on a Map
 */
public class DataEntity extends AbstractEntity
{
	private static final long serialVersionUID = 1060707187295905344L;
	private EntityMetaData entityMetaData;
	private Map<String, Object> values = UnifiedMap.newMap();
	private String idAttributeName = null;

	public DataEntity()
	{
	}

	public DataEntity(Entity other)
	{
		set(other);
	}

	public DataEntity(String idAttributeName)
	{
		this.idAttributeName = idAttributeName;
	}

	public DataEntity(Map<String, Object> values)
	{
		this.values = new UnifiedMap<>(values);
	}

	public DataEntity(String attributeName, Object value)
	{
		values.put(attributeName, value);
	}

	public DataEntity(EntityMetaData metaData)
	{
		this.entityMetaData = metaData;
		this.idAttributeName = entityMetaData.getIdAttribute().getName();
	}

	public Map<String, Object> getValueMap()
	{
		return values;
	}

	@Override
	public Object get(String attributeName)
	{
		return values.get(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		values.put(attributeName, value);
	}

	@Override
	public void set(Entity other, boolean strict)
	{
		for (String attributeName : other.getAttributeNames())
		{
			set(attributeName, other.get(attributeName));
		}
	}

	@Override
	public Object getIdValue()
	{
		if (getIdAttributeName() == null)
		{
			return null;
		}

		return get(getIdAttributeName());
	}

	@Override
	public String getLabelValue()
	{
		return null;
	}

	public String getIdAttributeName()
	{
		return idAttributeName;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		if (entityMetaData != null)
		{
			return Iterables.transform(entityMetaData.getAttributes(), new Function<AttributeMetaData, String>()
			{
				@Override
				public String apply(AttributeMetaData input)
				{
					return input.getName();
				}
			});
		}
		return values.keySet();
	}

	@Override
	public List<String> getLabelAttributeNames()
	{
		return Collections.singletonList(entityMetaData.getLabelAttribute().getName());
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}
}
