package com.apo.order;
/********************************************************************
* @(#)OrderTableModel.java 1.00 20110210
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* OrderTableModel: Extends AbstractTableModel to display order info.
* This model is shared by the contact details panel and the order
* reports.
*
* @author Rick Salamone
* @version 1.00, 20110210 rts created from RawTableModel
* @version 1.01, 20110403 rts added updateAll & update
* @version 1.02, 20110210 rts added interest set to filter orders
*******************************************************/
import com.apo.contact.Raw;
import com.shanebow.dao.OrderID;
import com.shanebow.ui.table.AbstractSavableTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import javax.swing.event.TableModelEvent;

public class OrderTableModel
	extends AbstractSavableTableModel
	implements com.shanebow.dao.table.SortableTableModel
	{
	private static final ArrayList<OrderTableModel> _all = new ArrayList<OrderTableModel>();
	public static void updateAll(Order aOrder)
		{
		for ( OrderTableModel model : _all )
			model.update(aOrder);
		}

	public final void update(Order aOrder)
		{
		int index = indexOf(aOrder);
		if ( index >= 0 )
			{
			if ( inInterestSet(aOrder))
				set(index, aOrder);
			else
				removeRow(index);
			}
		}

	public void setInterestSet( OrderStatus[] aInterestSet )
		{
		fInterestSet = aInterestSet;
		}
	public boolean inInterestSet(Order aOrder)
		{
		if ( fInterestSet == null ) return true;
		OrderStatus status = aOrder.status();
		for ( OrderStatus os : fInterestSet )
			if ( status.equals(os)) return true;
		return false;
		}

	private int[]       fFields = { 0, 1 }; // the field for each column
	private final List<Order> fRows = new Vector<Order>(); // the row data
	private OrderStatus[] fInterestSet; // which statuses does table diplay? null -> all

	public OrderTableModel(int[] aFields)
		{
		super();
		fFields = aFields;
		_all.add(this);
		}

	public OrderTableModel()
		{
		super();
		_all.add(this);
		}

	void setFields(int[] aFields)
		{
		fFields = aFields;
		fireTableStructureChanged();
		}

	public String getLastError() { return Order.DAO.getLastError(); }

	public boolean fetch(int aMaxRecords, String aQuery)
		{
System.out.println("order fetch: " + aQuery);
		fRows.clear();
		fireTableChanged(null); // Tell listeners the data is gone
		boolean successful = Order.DAO.fetch(fRows, aMaxRecords, aQuery);
		fireTableChanged(null); // Tell listeners new data has arrived.
		return successful;
		}

	public boolean fetch(Raw contact)
		{
		fRows.clear();
		fireTableChanged(null); // Tell listeners the data is gone
		if ( contact == null ) return true;
		boolean successful = Order.DAO.fetch(fRows, contact.id());
		fireTableChanged(null); // Tell listeners new data has arrived.
		return successful;
		}

	public void reset(List<Order> aOrderList)
		{
		fRows.clear();
		fireTableChanged(null); // Tell listeners the data is gone
		fRows.addAll(aOrderList);
		fireTableChanged(null); // Tell listeners new data has arrived.
		}

	public void add( int row, Order order )
		{
		fRows.add(row, order);
		fireTableRowsInserted(row, row);
		}

	public int indexOf(Order order) { return fRows.indexOf(order); }

	public Order get(int row) { return fRows.get(row); }

	public Order set(int row, Order order)
		{
		Order it = fRows.set(row, order);
		fireTableRowsUpdated(row, row);
		return it;
		}

	public Order removeRow(int row)
		{
		Order order = fRows.remove(row);
		fireTableRowsDeleted(row, row);
		return order;
		}

	@SuppressWarnings("unchecked") // we explicitly check "isAssignableFrom"
	public void sort(final int aSortColumn, final boolean aIsAscending )
		{
		final int ascend = aIsAscending? 1 : -1;
		final int sortField = fFields[aSortColumn];
		Class sortClass = Order.getFieldClass(sortField);
		if ( Comparable.class.isAssignableFrom(sortClass))
{
System.out.println("DataField sort");
			Collections.sort(fRows, new Comparator<Order>()
				{
				public int compare(Order r1, Order r2)
					{
					try
						{
						Comparable c1 = (Comparable)r1.get(sortField);
						Comparable c2 = (Comparable)r2.get(sortField);
						return ascend * c1.compareTo(c2);
						}
					catch (Exception e) { return 0; }
					}
				});
}
		else
{
System.out.println("String sort");
		Collections.sort(fRows,new Comparator<Order>()
			{
			public int compare(Order r1, Order r2)
				{
				return ascend
				       * r1.get(sortField).toString().compareTo(r2.get(sortField).toString());
				}
			});
}
		fireTableDataChanged();
		}

	@Override public int getColumnCount() { return fFields.length; }

	@Override public String getColumnName(int column)
		{
		return Order.getLabelText(fFields[column]);
		}

	@Override public Class getColumnClass(int column)
		{
		return Order.getFieldClass(fFields[column]);
		}

	@Override public boolean isCellEditable(int row, int column) { return false; }

	@Override public int getRowCount() { return fRows.size(); }

	@Override public Object getValueAt(int row, int col)
		{
		int field = fFields[col];
		return fRows.get(row).get(field);
		}
	}
