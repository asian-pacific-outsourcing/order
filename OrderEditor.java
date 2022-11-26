package com.apo.order;
/********************************************************************
* @(#)OrderEditor.java 1.00 20110213
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* OrderEditor: A panel for editing and validating the fields of a
* Contact record.
*
* Programming Notes: field represents the ContactField # which is the
* the field's index 
*
* @author Rick Salamone
* @version 1.00 20110213 rts created based upon RawEditor
*******************************************************/
import com.apo.contact.Source;
import com.shanebow.dao.*;
import com.shanebow.dao.DataFieldException;
import com.shanebow.dao.edit.FieldEditor;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.layout.LabeledPairLayout;
import com.shanebow.util.SBLog;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class OrderEditor
	extends JPanel
	{
	private static final double DEFAULT_COMM = 1.0; // percent commission

	private int[]        fFields;        // the Order Field Numbers on the form
	private JComponent[] fEditors;       // a field editor for each field
	private Order        fOrderUnedited; // the model object for a contact's raw data
	private boolean      fDirty;
	private boolean      fCommEdited; // true when user has altered default comm

	public OrderEditor(int[] fields)
		{
		super();

		JPanel center = this; // if fewer than 10 fields use one panel
		fFields = fields;
		int numFields = fFields.length;
		fEditors = new JComponent[numFields];

		for ( int ffi = 0; ffi < numFields; ffi++ )
			{
			JComponent ed;
			int cfn = fFields[ffi];
			ed = Order.getEditor(cfn);
			fEditors[ffi] = ed;
			ed.addKeyListener(keyListener);
			((FieldEditor)ed).addActionListener(actionListener);
			ed.setToolTipText(Order.getToolTip(cfn));
			if(cfn == Order.COST || cfn == Order.QTY || cfn == Order.COMM)
				((JTextComponent)ed).getDocument().addDocumentListener(docListener);
			}
		addFields( center, 0, numFields );
		if ( center != this )
			add( center, BorderLayout.CENTER );
// setEnabled( Order.COMM, false );
		}

	public void setOrder( Order aOrder )
		{
		fOrderUnedited = aOrder;
		if ( fOrderUnedited == null )
			clearFields();
		else for ( int i = 0; i < fFields.length; i++ )
			{
			int rfn = fFields[i];
			_set(rfn, fOrderUnedited.get(rfn));
			}
		calculate();
		fCommEdited = false;
		getEditorComponent(Order.COMM).setBackground(Color.WHITE);
		setDirty(false);
		}

	private void clearFields()
		{
		for ( JComponent field : fEditors )
			((FieldEditor)field).clear();
		setDirty(false);
		fEditors[0].requestFocusInWindow();
		}

private boolean fSetting;
	private void _set( int aRFN, DataField aValue )
		{
fSetting = true;
		FieldEditor ed = getFieldEditor(aRFN);
		if ( ed != null ) ed.set(aValue);
fSetting = false;
		}

	public final Order getUnedited() { return fOrderUnedited; }

	private final ActionListener actionListener = new ActionListener()
		{
		@Override public final void actionPerformed(ActionEvent e)
			{
			setDirty(true);
			}
		};

	private final DocumentListener docListener = new DocumentListener()
		{
		@Override public void changedUpdate(DocumentEvent e) {} // calculate();
		@Override public void insertUpdate(DocumentEvent e)  { calculate(); }
		@Override public void removeUpdate(DocumentEvent e) { calculate(); }
		};

	private final void calculate()
		{
if ( fSetting ) return;
		try
			{
			int qty = ((Qty)get(Order.QTY)).get();
			int pps = ((USD)get(Order.COST)).get();
			int totalCost = qty * pps;
			int comm = fCommEdited? ((USD)get(Order.COMM)).get()
			         : (int)((totalCost * DEFAULT_COMM) / 100.00);
System.out.format("calc qty %d * pps %d = %d\n", qty, pps, totalCost);
			_set(Order.COMM, new USD(comm));
			_set(Order.TOTAL, new USD(totalCost + comm));
			}
		catch (Exception e) { System.out.println("Order editor calc: " + e); }
		}

	private final KeyAdapter keyListener = new KeyAdapter()
		{
		@Override public final void keyTyped(KeyEvent e)
			{
			setDirty(true);
			if ( !fCommEdited && e.getSource().equals(getEditorComponent(Order.COMM)))
				{
				fCommEdited = true;
				getEditorComponent(Order.COMM).setBackground(Color.YELLOW);
				System.out.println("Comm dirty");
				}
			}
		};

	protected void setDirty(boolean on) { fDirty = on; }

	public boolean isDirty() { return fDirty; }

	private void addFields( JPanel p, int first, int last )
		{
		p.setLayout( new LabeledPairLayout());
		for ( int i = first; i < last; i++ )
			{
			int cfn = fFields[i];
			p.add(new JLabel(Order.getLabelText(cfn), JLabel.RIGHT), "label");
			p.add(fEditors[i], "field");
			}
		}

	private int indexOf( int aRFN )
		{
		for ( int i = 0; i < fFields.length; i++ )
			if ( fFields[i] == aRFN ) return i;
		return -1;
		}

	final public boolean includes( int aRFN )
		{
		return indexOf(aRFN) >= 0;
		}

	protected final JComponent getEditorComponent(int aRFN)
		{
		int ffi = indexOf(aRFN);
		return (ffi >= 0)? fEditors[ffi] : null;
		}

	private final FieldEditor getFieldEditor(int aRFN)
		{
		JComponent entryField = getEditorComponent(aRFN);
		return (entryField instanceof FieldEditor) ?
			(FieldEditor)entryField : null;
		}

	private void clearField( int aRFN )
		{
		JComponent entryField = getEditorComponent(aRFN);
		if ( entryField instanceof FieldEditor)
			((FieldEditor)entryField).clear();
		}

	public final Order getEditedOrder()
		{
		try
			{
			Security symbol = (Security)get(Order.SYMBOL);
			if ( symbol.isEmpty()) throw new DataFieldException("You must specify a security");
			return new Order(
				(OrderID)get(Order.ID),
				(ContactID)get(Order.RAWID),
				(When)get(Order.OPEN),
				symbol,
				(Qty)get(Order.QTY),
				(USD)get(Order.COST),
				(Source)get(Order.AO),
				(Source)get(Order.LO),
				(OrderStatus)get(Order.STATUS),
				(isDirty()? Order.DAO.getServerTime() : (When)get(Order.ACTIVITY)),
				(Comment)get(Order.NOTES),
				(USD)get(Order.COMM),
				(USD)get(Order.FEES));
			}
		catch ( Exception e )
			{
			SBDialog.inputError( e.getMessage());
			}
		return null;
		}

	private DataField get( int aRFN )
		throws DataFieldException
		{
		try
			{
			JComponent entryField = getEditorComponent(aRFN);
			if ( entryField == null ) // return blank
				return (fOrderUnedited == null)? Order.parse(aRFN, "") : fOrderUnedited.get(aRFN);
			if ( entryField instanceof FieldEditor)
				return ((FieldEditor)entryField).get();
			throw new DataFieldException ( "Field #" + aRFN + "does not implement FieldEditor");
			}
		catch (DataFieldException e ) { throw e; }
		}

	public void setEnabled( int aRFN, boolean on )
		{
		JComponent entryField = getEditorComponent(aRFN);
		if ( entryField == null ) return;
		if ( entryField instanceof JTextComponent )
			((JTextComponent)entryField).setEditable(on);
		else entryField.setEnabled(on);
		}

	protected boolean isBlank( int aRFN )
		throws DataFieldException
		{
		if ( !includes(aRFN)) // user cannot be held responsible
			return false;      // for field that's not on the form
		DataField df = get(aRFN);
		return (df == null) || df.isEmpty();
		}

	protected final void log( String fmt, Object... args )
		{
		SBLog.write( getClass().getSimpleName(), String.format( fmt, args ));
		}
	} // 178
