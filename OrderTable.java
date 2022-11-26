package com.apo.order;
/********************************************************************
* @(#)OrderTable.java 1.00 20110210
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* OrderTable: A extends JTable to display orders with sorting
* and mouse processing.
*
* @author Rick Salamone
* @version 1.00, 20110210 rts created from RawTable
* @version 1.01, 20110421 rts OrderStaus & renderer now in order package
*******************************************************/
import com.apo.contact.Raw;
import com.apo.order.Order;
import com.shanebow.dao.table.DFTable;
import com.shanebow.dao.table.ConfigurableTable;
import com.shanebow.util.SBProperties;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

public class OrderTable
	extends DFTable
	implements ConfigurableTable
	{
	private final String fPropertyPrefix;
	private final OrderTablePopup   fOrderTablePopup;
	private Order fSelected;

	public OrderTable(OrderTablePopup aOrderTablePopup)
		{
		this(aOrderTablePopup, "call.order.list.");
		}

	public OrderTable(OrderTablePopup aOrderTablePopup, String aPropertyPrefix)
		{
		super(new OrderTableModel());
		fPropertyPrefix = aPropertyPrefix;
		fOrderTablePopup = aOrderTablePopup;
		configure();

		setDefaultRenderer( OrderStatus.class, new ShowOrderStatus());
		setDropMode(DropMode.INSERT_ROWS);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Install a mouse listener in the Table itself for double clicks
		if ( SBProperties.getInstance().getBoolean("app.order.edit", false ))
			addMouseListener( new MouseAdapter()
				{
				public void mouseClicked(MouseEvent e)
					{
					if ( e.getClickCount() > 1 ) onDoubleClick();
					} 
				});

		// Use a scrollbar, because there are many columns
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
		setFillsViewportHeight( true );
		}

	private void onDoubleClick()
		{
		int row = getSelectionModel().getLeadSelectionIndex();
		if ( row >= 0 )	// clicked on a contact, row will be -1 for header/empty table
			{
			if ( fOrderTablePopup != null )
				{
				// @TODO fix DlgContact to work like DlgEditOrder!!
				if ( fOrderTablePopup instanceof DlgEditOrder )
					fOrderTablePopup.setVisible(true);
				else
					fOrderTablePopup.setOrder(fSelected);
				}
			}
		}

	// implement ListSelectionListener to get selected row
	@Override public void valueChanged( ListSelectionEvent e )
		{
		super.valueChanged(e);
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		if ( lsm.getValueIsAdjusting())
			return;

		OrderTableModel model = (OrderTableModel)getModel();
		// Find out which indexes are selected
		int i = lsm.getLeadSelectionIndex();
		if ( lsm.isSelectionEmpty())
			{
			if ( model.getRowCount() > 0 )
				lsm.setSelectionInterval(0,0);
			else
				{
				selected(null);
				}
			return;
			}
		else // if ( lsm.isSelectedIndex(i))
			{
			selected( model.get(i));
			}
		}

	private void selected( Order aOrder )
		{
		fSelected = aOrder;
		if ( fOrderTablePopup instanceof DlgEditOrder )
			fOrderTablePopup.setOrder( aOrder );
		}

	public final void fetch( Raw contact )
		{
		OrderTableModel model = (OrderTableModel)getModel();
		model.fetch(contact);
		selected( null );
		}

	public final void reset(List<Order> aOrderList)
		{
		OrderTableModel model = (OrderTableModel)getModel();
		model.reset(aOrderList);
		selected( null );
		}

	// Stuff to make the table configurable via a PreferencesEditor
	public final String getPropertyPrefix() { return fPropertyPrefix; }
	public final com.shanebow.dao.FieldMeta[] getAvailableFields() { return Order.meta; }
	public final void configure()
		{
		SBProperties props = SBProperties.getInstance();
		int[] fields = props.getIntArray(fPropertyPrefix + "fields", 0, 1, 2, 3, 4, 6);
		((OrderTableModel)getModel()).setFields(fields);
		int fontSize = props.getInt(fPropertyPrefix + "font.size", 12);
		setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
		}
	}
