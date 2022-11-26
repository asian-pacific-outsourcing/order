package com.apo.order;
/********************************************************************
* @(#)DlgAddOrder.java	1.13 10/04/07
* Copyright (c) 2010 by Richard T. Salamone, Jr. All rights reserved.
*
* DlgAddOrder: allows the user to add, edit or delete a contact.
*
* @author Rick Salamone
* @version 1.01 20100627 RTS position at lower right corner of screen
* @version 1.02 20100627 RTS responds to change in theme menu
* @version 1.03 20101005 RTS added history tab
* @version 1.04 20101006 RTS added touches
* @version 1.05 20101010 RTS added comments, cleaned up button bar
* @version 1.06 20101012 RTS added contact search for id feature
* @version 1.07 20101021 RTS using common DlgComment
*******************************************************/
import com.apo.contact.Raw;
import com.apo.contact.Source;
import com.apo.order.OrderStatus;
import com.shanebow.dao.*;
import com.apo.net.Access;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBDialog;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBProperties;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class DlgAddOrder
	extends JDialog
	implements OrderTablePopup
	{
	private static final String NEW_ORDER="Write Order";

	private final OrderEditor fOrderEditor;
	private Order fOrder;

	private final AbstractAction fSaveAction = new AbstractAction("Save")
		{
		// constructor code
			{
			putValue(SHORT_DESCRIPTION, "Save edits and close dialog");
			}

		@Override public void actionPerformed(ActionEvent e)
			{
			fOrder = fOrderEditor.getEditedOrder();
			if ( fOrder == null ) return; // invalid data
		System.out.println( "DlgOrderEdit got order id: " + fOrder.id() + " max " + Integer.MAX_VALUE);
			setVisible(false);
			fOrderEditor.setOrder(null);
	  		}
		};

	public DlgAddOrder()
		{
		super((Frame)null, LAF.getDialogTitle(NEW_ORDER), true);

		SBProperties props = SBProperties.getInstance();
		int[] fields = props.getIntArray("app.call.order.add.fields");
		fOrderEditor = new OrderEditor(fields)
			{
			protected final void setDirty(boolean on)
				{
				super.setDirty(on);
				fSaveAction.setEnabled(on);
				}
			};
		
		setBounds( props.getRectangle( "usr.order.add.bounds", 50,50,265,535));
		addComponentListener( new ComponentAdapter()
			{
			public void componentMoved(ComponentEvent e) { saveBounds(); }
			public void componentResized(ComponentEvent e) { saveBounds(); }
			});

		JPanel top = new JPanel(new BorderLayout());
		top.setBorder( LAF.getStandardBorder());
		top.add( fOrderEditor, BorderLayout.CENTER );
		top.add( LAF.getCommandRow(new JButton(fSaveAction)), BorderLayout.SOUTH );
		setContentPane(top);
		LAF.addUISwitchListener(this);
		}

	private void saveBounds()
		{
		SBProperties.getInstance().setProperty( "usr.order.add.bounds", getBounds());
		}

	public void setOrder(Order aOrder)
		{
		String title = ((aOrder == null)? NEW_ORDER : ("Edit " + aOrder.title()));
		setTitle( LAF.getDialogTitle(title));
		fOrderEditor.setOrder(aOrder);
		}

	public Order getOrder() { return fOrder; }

	public void editNewOrder(Raw contact)
		{
		fOrder = null;
		When now = Order.DAO.getServerTime();
		Order defaultValues = new Order( OrderID.NEW_ORDER, contact.id(),
			now,
			Security.XX, Qty.ONE_THOUSAND, USD.ONE_DOLLAR,
			(Access._role == Access.AO)? Access.usrID() : Source.XX,
			(Access._role == Access.LO)? Access.usrID() : Source.XX,
			OrderStatus.WRITTEN, now, Comment.BLANK, USD.ZERO, USD.ZERO );
		String title = NEW_ORDER;
		setTitle( LAF.getDialogTitle(title));
		fOrderEditor.setOrder(defaultValues);
		setVisible(true);
		}

	protected final void log ( String msg ) { SBLog.write( msg ); }
	}
