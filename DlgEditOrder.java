package com.apo.order;
/********************************************************************
* @(#)DlgEditOrder.java	1.13 20100627
* Copyright (c) 2010-2011 by Richard T. Salamone, Jr. All rights reserved.
*
* DlgEditOrder: allows the user to add, edit or delete a contact.
*
* @author Rick Salamone
* @version 1.01 20100627 RTS position at lower right corner of screen
* @version 1.02 20100627 RTS responds to change in theme menu
* @version 1.03 20101005 RTS added history tab
* @version 1.04 20101006 RTS added touches
* @version 1.05 20101010 RTS added comments, cleaned up button bar
* @version 1.06 20101012 RTS added contact search for id feature
* @version 1.07 20101021 RTS using common DlgComment
* @version 2.00 20110403 RTS major overhaul to handle actual order flow
*******************************************************/
import com.apo.apps.caller.CallerGUI;
import com.apo.contact.Raw;
import com.apo.contact.Dispo;
import com.apo.contact.Source;
import com.apo.contact.touch.TouchCode;
import com.shanebow.dao.*;
import com.apo.net.Access;
import com.apo.employee.Role;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBDialog;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBProperties;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public final class DlgEditOrder
	extends JDialog
	implements OrderTablePopup
	{
	private final OrderEditor fOrderEditor;
	private final OrderAction[] fActions;

	public DlgEditOrder(JFrame f)
		{
		super(f, LAF.getDialogTitle("Edit Order"), true);

		SBProperties props = SBProperties.getInstance();
		int[] fields = props.getIntArray("app.call.order.edit.fields");
		fOrderEditor = new OrderEditor(fields);

		int uid = Access.getUID();
		Dispo dispo;
		if ( uid == 13 ) dispo = Dispo.RMP; // JW
		else if ( uid == 8 || uid == 7 ) dispo = Dispo.ADP; // Madman
		else // if ( Access.getRole().access() == Access.AO )
			dispo = Dispo.AOP;
		OrderStatus[] states = OrderStatus.nextStates( dispo );
System.out.println("states size: " + states.length);
		fActions = new OrderAction[states.length];
		int j = 0;
		for ( OrderStatus os : states )
			fActions[j++] = new OrderAction(os);
System.out.println("fActions length: " + fActions.length);
		setBounds( props.getRectangle( "usr.order.edit.bounds", 22,32,255,335));
		addComponentListener( new ComponentAdapter()
			{
			public void componentMoved(ComponentEvent e) { saveBounds(); }
			public void componentResized(ComponentEvent e) { saveBounds(); }
			});

		JPanel top = new JPanel(new BorderLayout());
		top.setBorder( LAF.getStandardBorder());
		top.add( fOrderEditor, BorderLayout.CENTER );
		JButton[] buttons = new JButton[fActions.length];
		for ( int i = 0; i < buttons.length; i++ )
			buttons[i] = fActions[i].button();
		top.add( LAF.getCommandRow(buttons), BorderLayout.SOUTH );
		setContentPane(top);
		fOrderEditor.setEnabled(Order.OPEN, false);
		fOrderEditor.setEnabled(Order.ACTIVITY, false);
		LAF.addUISwitchListener(this);
		}

	private void saveBounds()
		{
		SBProperties.getInstance().setProperty( "usr.order.edit.bounds", getBounds());
		}

	public void setOrder(Order aOrder)
		{
		if ( aOrder == null )
			{
			setVisible(false);
			return;
			}

		for ( OrderAction action : fActions )
			action.setVisibleFor(aOrder.status());
		String title = "Edit " + aOrder.title();
		setTitle( LAF.getDialogTitle(title));
		fOrderEditor.setOrder(aOrder);
		}

	protected final void log ( String msg ) { SBLog.write( msg ); }

	class OrderAction extends AbstractAction
		{
		private final OrderStatus fOrderStatus;
		private final JButton fButton;

		public OrderAction(OrderStatus aOrderStatus)
			{
			super(aOrderStatus.name());
			fOrderStatus = aOrderStatus;
			fButton = new JButton(this);
		System.out.println( "action created: " + fOrderStatus.name() );
			}

		@Override public void actionPerformed(ActionEvent e)
			{
			Order edits = new Order(fOrderEditor.getEditedOrder(), fOrderStatus );
			if ( edits == null ) return; // invalid data
			System.out.println( "action: " + edits.status().id() + " " + edits);
			try
				{
				Order.DAO.update( edits, Order.DAO.getServerTime(), Access.empID(),
					TouchCode.MODORDER, getChanges(edits));
				CallerGUI gui = CallerGUI.getInstance();
				Raw raw = gui.getUnedited();
				setVisible(false);
				When callback = new When( Raw.DAO.getServerTime() + fOrderStatus.callbackDelay());
				gui.finishUp( raw, null, fOrderStatus.dispo(), callback, fOrderStatus.name());
				OrderTableModel.updateAll(edits);
				}
			catch (Exception ex) { SBDialog.error("Data Access Error", ex.getMessage()); }
//			closeDialog();
			}

		private Comment getChanges(Order edits)
			{
			Order was = fOrderEditor.getUnedited();
			int[] dirtyFields = edits.unequalFields(was);
			if ( dirtyFields.length == 0 )
				return Comment.BLANK;
			String changes = "";
			for ( int field : dirtyFields )
				{
				if ( field == Order.ACTIVITY || field == Order.STATUS )
					continue;
				changes += (changes.isEmpty()? "" : ",")
				       + Order.getLabelText(field) + was.get(field) + "-" + edits.get(field);
				}
			if ( changes.length() >= Comment.MAX_CHARS )
				changes = changes.substring(0,Comment.MAX_CHARS-4) + "...";
			Comment comment;
			try { comment = Comment.parse(changes); }
			catch (Exception ex) { comment = Comment.BLANK; }
System.out.println( "DlgOrderEdit got order id: "
+ edits.id() + " mod: " + changes );
			return comment;
			}

		private void closeDialog()
			{
			Container parent = fButton.getParent();
			while ( !(parent instanceof JDialog))
				parent = parent.getParent();
			((JDialog)parent).setVisible(false);
			}

		public final JButton button() { return fButton; }
		private final void setBtnVisible(boolean on) { fButton.setVisible(on); }
		public final void setVisibleFor(OrderStatus aOrderStatus)
			{
			setBtnVisible(fOrderStatus.equals(OrderStatus.CANCEL) || fOrderStatus.reachableFrom(aOrderStatus));
			}
		}
	}
