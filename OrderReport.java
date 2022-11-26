package com.apo.order;
/********************************************************************
* @(#)OrderReport.java 1.00 10/05/24
* Copyright (c) 2010 by Richard T. Salamone, Jr. All rights reserved.
*
* OrderReport: A panel that accepts SQL input and displays the results
* of the SQL in a table.
*
* @author Rick Salamone
* @version 1.00, 20100524 rts for the SQL Editor program
* @version 1.50, 20101030 rts now makes requests to application server
* @version 1.51, 20110203 rts decoupled table from report
* @version 1.52, 20110404 rts contructor takes an OrderTablePopup
*******************************************************/
import com.shanebow.util.SBLog;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class OrderReport
	extends JPanel
	{
	private static final String CMD_SAVE = "Save";

	private final OrderFilters    fFilters = new OrderFilters();
	private final OrderTableModel fModel;
	private final OrderTable      fTable;
	private final JLabel          fStatusBar = new JLabel("");
	private final JButton btnFetch = new JButton("Fetch");

	public OrderReport(OrderTablePopup aOrderTablePopup)
		{
		super ( new BorderLayout());
		fTable = new OrderTable(aOrderTablePopup, "usr.order.report.");
		fTable.makeConfigurable();
		fModel = (OrderTableModel)fTable.getModel();
		JSplitPane report = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT );
		report.setLeftComponent( fFilters );
		report.setRightComponent( resultsPanel());
		report.setDividerLocation( 230 );
		report.setOneTouchExpandable(true);
		report.setContinuousLayout(true);

		add(report, BorderLayout.CENTER);
		add(statusPanel(), BorderLayout.SOUTH);
		}

	private JComponent statusPanel()
		{
		btnFetch.addActionListener( new ActionListener()
			{
			public void actionPerformed(ActionEvent e) { onFetch(); }
			});
		JButton btnSave = new JButton(CMD_SAVE);
		btnSave.addActionListener( new ActionListener()
			{
			public void actionPerformed(ActionEvent e) { fModel.saveAs(fTable); }
			});

		JPanel p = new JPanel(new BorderLayout());
		p.add( btnFetch, BorderLayout.WEST );
		p.add( fStatusBar, BorderLayout.CENTER );
		p.add( btnSave, BorderLayout.EAST );
		return p;
		}

	private JComponent resultsPanel()
		{
		JScrollPane scroller = new JScrollPane(fTable);
		scroller.setBorder( BorderFactory.createLoweredBevelBorder());
		return scroller;
		}

	public void onFetch()
		{
		String sql = fFilters.getSQL();
		int show = fFilters.getMaxShowCount();
		SBLog.write( "SQL: '" + sql + "'" );
		if ( fModel.fetch( show, sql ))
				{
				fStatusBar.setText( "Retrieved " + fModel.getRowCount() + " items" );
				}
			else fStatusBar.setText( "ERROR: " + fModel.getLastError());
		}

/*******************
	private JComponent toggleDetails(Component dlgContact, JTable table)
		{
		if ( !(dlgContact instanceof PropertyChangeListener))
			throw new IllegalArgumentException("dlgContact must be a prop change listener");
		table.addPropertyChangeListener((PropertyChangeListener)dlgContact);
		dlgContact.addPropertyChangeListener(new PropertyChangeListener()
			{
			public void propertyChange(PropertyChangeEvent evt)
				{
				String property = evt.getPropertyName();
				// SBLog.write( "Controls", "received propertyChange " + property );
				if ( property.equals("DELETED CONTACT")
				||   property.equals("UPDATED CONTACT"))
					btnFetch.doClick();
				}
			});
		ToggleDetails tbDetails = new ToggleDetails(dlgContact);
		table.addPropertyChangeListener(Raw.SELECTED_PROPERTY, tbDetails );
		return tbDetails;
		}
*******************/
	}
