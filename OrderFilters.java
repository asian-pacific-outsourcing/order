package com.apo.order;
/********************************************************************
* @(#)OrderFilters.java 1.00 20110213
* Copyright 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* OrderFilters: GUI to build querries for the Orders report.
*
* @author Rick Salamone
* @version 1.00 20110213 rts created based on RawFilters
*******************************************************/
import com.apo.contact.Raw;
import com.apo.contact.Source;
import com.apo.contact.edit.EditSource;
import com.shanebow.dao.*;
import com.shanebow.dao.edit.*;
import com.apo.employee.Role;
import com.shanebow.ui.calendar.MonthCalendar;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.layout.LabeledPairPanel;
import com.shanebow.ui.LAF;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBProperties;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public final class OrderFilters
	extends JPanel
	{
	protected final MonthCalendar calendar = new MonthCalendar();
	private final JCheckBox       chkOpen = new JCheckBox("Opened");
	private final JCheckBox       chkAct = new JCheckBox("Activity");
	private final EditDateRange   openDateRange = new EditDateRange(EditDateRange.VERTICAL);
	private final EditDateRange   actDateRange = new EditDateRange(EditDateRange.VERTICAL);
	private final EditSource      cbAO = new EditSource();
	private final EditSource      cbLO = new EditSource();
	private final SelectOrderStatus cbOrderState = new SelectOrderStatus();
	private final EditSecurity    cbSymbol = new EditSecurity();
	private final EditQty         tfQty = new EditQty();
	private final JTextField      edRawID = new JTextField();

	private final SelectRelationalOperator qtyOp = new SelectRelationalOperator();

	public OrderFilters()
		{
		super( new BorderLayout());

		calendar.addPropertyChangeListener(
			MonthCalendar.TIMECHANGED_PROPERTY_NAME, openDateRange);
		calendar.addPropertyChangeListener(
			MonthCalendar.TIMECHANGED_PROPERTY_NAME, actDateRange);
		calendar.setOpaque(false);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(calendar);
		splitPane.setBottomComponent(new JScrollPane(filtersPanel()));
		splitPane.setDividerLocation(190); //XXX ignored in some releases bug 4101306
		add(splitPane, BorderLayout.CENTER);
		}

	private JComponent filtersPanel()
		{
		LabeledPairPanel p = new LabeledPairPanel();  // "Filters" );
		p.setBorder(LAF.getStandardBorder());

		p.addRow(new Section("When"), new JLabel());
		p.addRow( chkOpen, openDateRange );
		p.addRow( chkAct, actDateRange );

		p.addRow(new Section(), new Section());
		p.addRow(new Section("Owner"), new JLabel());
		p.addRow(   "AO", cbAO );
		p.addRow(   "LO", cbLO );

		p.addRow(new Section(), new Section());
		p.addRow(new Section("Other"), new JLabel());
		p.addRow( "State", cbOrderState );
		p.addRow( "Symbol", cbSymbol );
		p.addRow( "Quantity", qtyOp, tfQty );
		p.addRow( "Client #", edRawID);

		tfQty.set(Qty.ZERO);
		qtyOp.setSelectedItem(">=");
		return p;
		}

	public int getMaxShowCount()
		{
		return 500;
//		try { return Integer.parseInt((String)cbShow.getSelectedItem()); }
//		catch (Exception e) { return -1; }
		}

	public String getSQL()
		{
		return "SELECT * FROM " + Order.DB_TABLE
		          + "\n WHERE " + getWhereClause()
		          + "\n ORDER BY " + Order.dbField(Order.ACTIVITY) + " DESC";
		}

	public String getWhereClause()
		{
		long openDates[] = openDateRange.getDateRange();
		long actDates[] = actDateRange.getDateRange();
		Source aoID;
		Source loID;
		OrderStatus status;
		Security symbol;
		Qty qty;
		ContactID rawID = null;
		String rawIDString = edRawID.getText().trim();
		if ( !rawIDString.isEmpty())
			try { rawID = ContactID.parse(rawIDString); }
			catch (Exception ex)
				{
				SBDialog.inputError( "Malformed Contact ID\n" + ex.getMessage());
				return null;
				}
		try
			{
			aoID = cbAO.get();
			loID = cbLO.get();
			status = cbOrderState.get();
			symbol = cbSymbol.get();
			qty = tfQty.get();
			}
		catch (Exception e)
			{
			SBDialog.inputError(e.toString());
			return null;
			}

		String it = "qty "+ qtyOp.getSelectedItem() + " " + qty.dbRepresentation();
		if ( aoID != Source.XX )
			it += " AND aoID = " + aoID.dbRepresentation();
		if ( loID != Source.XX )
			it += " AND loID = " + loID.dbRepresentation();
		if ( status != OrderStatus.XX )
			it += " AND status = " + status.dbRepresentation();
		if ( symbol != Security.XX )
			it += " AND symbol = " + symbol.dbRepresentation();
		if ( chkOpen.isSelected())
			it += " AND open BETWEEN " + openDates[0] + " AND " + openDates[1];
		if ( chkAct.isSelected())
			it += " AND activity BETWEEN " + actDates[0] + " AND " + actDates[1];
		if ( rawID != null )
			it += " AND rawID = " + rawID.dbRepresentation();
		return it;
		}
	}

class Section extends JLabel
	{
	public Section()
		{
		super( "<html><HR width=5000>" );
		}
	public Section(String title)
		{
		super( "<html><B>" + title );
		}
	}
