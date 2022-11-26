package com.apo.order;
/********************************************************************
* @(#)Order.java 1.00 20110209
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* Order: The model object for a security buy or sell.
*
* @author Rick Salamone
* @version 1.00, 20110209 rts created
* @version 1.01, 20110402 rts added total
*******************************************************/
import com.shanebow.util.CSV;
import com.apo.contact.Source;
import com.apo.contact.edit.EditSource;
import com.shanebow.dao.*;
import com.shanebow.dao.edit.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.swing.JComponent;

public final class Order
	{
	public static OrderDAO DAO;
	public static final String DB_TABLE="redro";

	public static final int        ID = 0;
	public static final int     RAWID = 1; // FK raw contact
	public static final int      OPEN = 2;
	public static final int    SYMBOL = 3; // FK securities
	public static final int       QTY = 4;
	public static final int      COST = 5;
	public static final int        AO = 6; // FK emp
	public static final int        LO = 7; // FK emp
	public static final int    STATUS = 8;
	public static final int  ACTIVITY = 9;
	public static final int     NOTES = 10;
	public static final int      COMM = 11;
	public static final int      FEES = 12;

	// NOTE!!!!!! Remaining fields not stored in database!!!!
	public static final int NUM_DB_FIELDS = FEES + 1;

	public static final int     NAME  = 13; // inner join
	public static final int     PRICE = 14; // web look up
	public static final int      GAIN = 15; // calculated
	public static final int     TOTAL = 16;

	static final FieldMeta[] meta =
		{    // field #, label, dbFieldName, class / editor class, tooltip
		new FieldMeta( ID, "Order #", "id", OrderID.class,
		                           EditOrderID.class, "Unique Order Number" ),
		new FieldMeta( RAWID, "Client #", "rawID", ContactID.class,
		                           EditContactID.class, "Contact who owns this order" ),
		new FieldMeta( OPEN, "Written", "open", When.class,
		                           EditWhen.class, "When order written" ),
		new FieldMeta( SYMBOL, "Symbol", "symbol", Security.class,
		                           EditSecurity.class, "Security ticker symbol" ),
		new FieldMeta( QTY, "Qty", "qty", Qty.class,
		                           EditQty.class, "Number of shares" ),
		new FieldMeta( COST, "Cost $", "cost", USD.class,
		                           EditUSD.class, "USD per share when ordered" ),
		new FieldMeta( AO, "AO", "aoID", Source.class,
		                           EditSource.class, "Account Opener" ),
		new FieldMeta( LO, "LO", "loID", Source.class,
		                           EditSource.class, "Account Loader" ),
		new FieldMeta( STATUS, "Status", "status", OrderStatus.class,
		                           ShowOrderStatus.class, "Order Status" ),
		new FieldMeta( ACTIVITY, "Activity", "activity", When.class,
		                           EditWhen.class, "Last status change date" ),
		new FieldMeta( NOTES, "Instruct", "notes", Comment.class,
		                           EditComment.class, "Client instructions" ),
		new FieldMeta( COMM, "Commission", "commission", USD.class,
		                           EditUSD.class, "Commission on entire order" ),
		new FieldMeta( FEES, "Fees", "fees", USD.class,
		                           EditUSD.class, "Fees per share when ordered" ),

		/************** The Contact name is an inner join field ************************/
		new FieldMeta( NAME, "Name", null, ContactName.class,
		                           ShowContactName.class, "Contact Name" ),

		/************** The Current price is an updating field *************************/
		new FieldMeta( PRICE, "Price $", null, USD.class,
		                           ShowUSD.class, "Current price per share" ),

		/************** The following are calculated fields ****************************/
		new FieldMeta( GAIN, "PNL", null, PNL.class,
		                           EditPNL.class, "Profit = Current Price - cost * qty" ),
		new FieldMeta( TOTAL, "Total", null, USD.class,
		                           ShowUSD.class, "Total = (cost+fees) * qty + commission" ),
		};
	public static final int  NUM_FIELDS = meta.length;

	public static Object getDefaultValue(int field) { return (field==ID)? "" : "0"; }
	public static int    getNumFields()        { return meta.length; }
	public static String getToolTip(int i)     { return meta[i].toolTip(); }
	public static String getLabelText(int i)   { return meta[i].label(); }
	public static String dbField(int i) { return meta[i].dbFieldName(); }
	public static JComponent getEditor(int i)  { return meta[i].editor(); }
	public static Class<? extends DataField> getFieldClass(int i)
		{
		return meta[i].getFieldClass();
		}
	public static int dbFieldIndex( String dbFieldName )
		{
		for ( int i = 0; i < NUM_DB_FIELDS; i++ )
			if ( dbFieldName.equalsIgnoreCase(meta[i].dbFieldName()))
				return i;
		return -1;
		}

	public static DataField parse( int fieldIndex, String value )
		throws DataFieldException
		{
		switch ( fieldIndex )
			{
			case ID:        return OrderID.parse(value);
			case RAWID:     return ContactID.parse(value);
			case OPEN:      return When.parse(value);
			case SYMBOL:    return Security.parse(value);
			case QTY:       return Qty.parse(value);
			case COST:      return USD.parse(value);
			case AO:
			case LO:        return Source.parse(value);
			case STATUS:    return OrderStatus.parse(value);
			case ACTIVITY:  return When.parse(value);
			case NOTES:     return Comment.parse(value);
			case COMM:      return USD.parse(value);
			case FEES:      return USD.parse(value);
			case NAME:      return ContactName.parse(value);
			case PRICE:     return USD.parse(value);
			case GAIN:      return PNL.parse(value);
			case TOTAL:     return USD.parse(value);
			}
		throw new DataFieldException( "" + fieldIndex + ": No parser available" );
		}

	public static DataField read( int fieldIndex, ResultSet rs, int rsCol )
		throws DataFieldException
		{
		switch ( fieldIndex )
			{
			case ID:        return OrderID.read( rs, rsCol);
			case RAWID:     return ContactID.read( rs, rsCol);
			case OPEN:      return When.read( rs, rsCol);
			case SYMBOL:    return Security.read( rs, rsCol);
			case QTY:       return Qty.read( rs, rsCol);
			case COST:      return USD.read( rs, rsCol);
			case AO:
			case LO:        return Source.read( rs, rsCol);
			case STATUS:    return OrderStatus.read( rs, rsCol);
			case ACTIVITY:  return When.read( rs, rsCol);
			case NOTES:     return Comment.read( rs, rsCol);
			case COMM:
			case FEES:      return USD.read( rs, rsCol);
			case NAME:      return ContactName.read( rs, rsCol);
			case PRICE:     return USD.read( rs, rsCol);
			case GAIN:      return PNL.read( rs, rsCol);
			case TOTAL:     return USD.read( rs, rsCol);
			}
		throw new DataFieldException("Invalid field index: " + fieldIndex );
		}

	private final DataField fFields[] = new DataField[meta.length];
	public DataField    get(int field)
		{
		if ( field < NUM_DB_FIELDS )
			return fFields[field];
		else if ( field == PRICE )
			return getPrice();
		else if ( field == GAIN )
			return pnl();
		else if ( field == TOTAL )
			return total();
		else return null;
		}

	public USD getPrice()
		{
		return (USD)fFields[COST]; // @TODO must implement
		}

	public PNL pnl()
		{
		try { return new PNL( getPrice(), (USD)fFields[COST], (Qty)fFields[QTY]); }
		catch (Exception e) { return PNL.ZERO; }
		}

	public USD total()
		{
		try
			{
			int cents = (((USD)fFields[COST]).get() + ((USD)fFields[FEES]).get())
		            * ((Qty)fFields[QTY]).get() + ((USD)fFields[COMM]).get();
			return new USD(cents);
			}
		catch (Exception e) { return USD.ZERO; }
		}

	public OrderID      id()          { return (OrderID)fFields[ID]; }
	public ContactID    rawID()       { return (ContactID)fFields[RAWID]; }
	public Security     symbol()      { return (Security)fFields[SYMBOL]; }
	public OrderStatus  status()      { return (OrderStatus)fFields[STATUS]; }

	public Order( OrderID aID, Order aOtherOrder )
		{
		for ( int i = 0; i < fFields.length; i++ )
			fFields[i] = aOtherOrder.fFields[i];
		fFields[ID] = aID;
		}

	public Order( Order aOtherOrder, OrderStatus aOrderStatus )
		{
		for ( int i = 0; i < fFields.length; i++ )
			fFields[i] = aOtherOrder.fFields[i];
		fFields[STATUS] = aOrderStatus;
		fFields[ACTIVITY] = DAO.getServerTime();
		}

	public Order( OrderID aID, ContactID aRawID, When aOpened,
		Security aSecurityID, Qty aQty, USD aCost, Source aAO, Source aLO,
		OrderStatus aStatus, When aActivity, Comment aComment, USD aComm, USD aFees )
		{
		fFields[ID] = aID;
		fFields[RAWID] = aRawID;
		fFields[OPEN] = aOpened;
		fFields[SYMBOL] = aSecurityID;
		fFields[QTY] = aQty;
		fFields[COST] = aCost;
		fFields[AO] = aAO;
		fFields[LO] = aLO;
		fFields[STATUS] = aStatus;
		fFields[ACTIVITY] = aActivity;
		fFields[NOTES] = aComment;
		fFields[COMM] = aComm;
		fFields[FEES] = aFees;
		}

	public Order(	String csv )
		throws DataFieldException
		{
		this(CSV.split(csv, NUM_DB_FIELDS));
		}

	public Order(	String[] value )
		throws DataFieldException
		{
		for ( int i = 0; i < NUM_DB_FIELDS; i++ )
			fFields[i] = parse( i, value[i] );
		}

	public Order(	ResultSet rs )
		throws DataFieldException
		{
		int rsCol = 0;
		for ( int i = 0; i < NUM_DB_FIELDS; i++ )
			fFields[i] = read( i, rs, i + 1 );
		// if this was the result of inner join on name...
		try { fFields[NAME] = read(NAME, rs, NAME+1); }
		catch (Exception e) {}
		}

	public String toCSV()
		{
		String csv = "";
		for ( int i = 0; i < NUM_DB_FIELDS; i++ )
			{
			if ( i > 0 ) csv += ",";
			csv += fFields[i].csvRepresentation();
			}
		return csv;
		}

	@Override public boolean equals(Object that)
		{
		return that != null && that instanceof Order
		    && ((Order)that).id().equals(this.id());
		}

	@Override public String toString() { return title(); }

	public String title()
		{
		return "Order #" + get(ID) + " " + get(QTY) + " " + get(SYMBOL) + " @ " + get(COST);
		}

	public int[] unequalFields(Order other)
		{
		int numDirty = 0;
		for ( int i = 0; i < NUM_DB_FIELDS; i++ )
			if ( i != ACTIVITY
			&&   !fFields[i].equals(other.fFields[i]))
				++numDirty;
		int[] it = new int[numDirty];
		for ( int i = 0, x = 0; i < NUM_DB_FIELDS; i++ )
			if ( i != ACTIVITY
			&&   !fFields[i].equals(other.fFields[i]))
				it[x++] = i;
		return it;
		}
	}
