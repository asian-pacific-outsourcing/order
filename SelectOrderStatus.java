package com.apo.order;
/********************************************************************
* @(#)SelectOrderStatus.java 1.00 11/01/27
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* SelectOrderStatus: A component for selecting an OrderStaus.
*
* @version 1.00 01/27/11
* @author Rick Salamone
*******************************************************/
import com.shanebow.dao.DataField;
import com.shanebow.dao.DataFieldException;
import com.shanebow.dao.edit.FieldEditor;
import java.sql.ResultSet;
import javax.swing.JComboBox;

public class SelectOrderStatus extends JComboBox
	implements FieldEditor
	{
	public SelectOrderStatus ()
		{
		super();
		for ( OrderStatus t : OrderStatus.getAll())
			addItem(t);
		}

	public void clear() {}

	public void set(String text)
		{
		try { setSelectedItem(OrderStatus.parse(text)); }
		catch (Exception e) { setSelectedIndex(0); }
		}

	public void set(DataField field)
		{
		try { setSelectedItem(field); }
		catch (Exception e) { setSelectedIndex(0); }
		}

	public OrderStatus get()
		throws DataFieldException
		{
		return (OrderStatus)getSelectedItem();
		}

	public boolean isEmpty() { return false; }
	}
