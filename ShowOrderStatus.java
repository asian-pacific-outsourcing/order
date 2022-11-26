package com.apo.order;
/********************************************************************
* @(#)ShowOrderStatus.java 1.00 20110406
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* ShowOrderStatus: A component for displaying order status: Simply extends
* JLabel to implement FieldEditor.
*
* @version 1.00 20110406
* @author Rick Salamone
*******************************************************/
import com.shanebow.dao.DataField;
import com.shanebow.dao.DataFieldException;
import com.shanebow.dao.edit.FieldEditor;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public final class ShowOrderStatus
	extends JLabel
	implements FieldEditor, TableCellRenderer
	{
	public ShowOrderStatus ()
		{
		super();
		setOpaque(true);
		setFont( new javax.swing.JList().getFont());
//		setForeground(Color.WHITE);
		}

	@Override public void addActionListener(java.awt.event.ActionListener al) {}

	public void clear() { _set("", getForeground());}

	public void set(DataField f)
		{
		if ( f == null || !(f instanceof OrderStatus)) { clear(); return; }
		OrderStatus status = (OrderStatus)f;
		_set(status.toString(), status.color()); }

	public void _set(String text, Color bg)
		{
		setText(text);
		setBackground(bg);
		}

	public DataField get()
		throws DataFieldException
		{
		return OrderStatus.parse( getText());
		}

	public boolean isEmpty() { return getText().trim().isEmpty(); }

	public Component getTableCellRendererComponent(
                            JTable table, Object aOrderStatus,
                            boolean isSelected, boolean hasFocus,
                            int row, int column)
		{
		OrderStatus status = (OrderStatus)aOrderStatus;
		if ( isSelected )
			{
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
			}
		else
			{
			setForeground(Color.BLACK);
			setBackground(status.color());
			}
		if ((status == null) || (status == OrderStatus.XX))
			clear();
setText(status.toString());
		return this;
		}
	}
