package com.apo.order;
/********************************************************************
* @(#)OrderStatus.java 1.00 20100210
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* OrderStatus: A combo box for managing the status of a client's order.
*
* @author Rick Salamone
* @version 1.00 20110210 rts
* @version 2.00 20110403 rts major rewrite to handle order flows per apo
* @version 2.01 20110403 rts added color - first iteration based on dispo
* @version 2.02 20110421 rts OrderStaus & renderer now in order package
* @version 2.03 20110424 rts added CANCEL as next state for 4 uid's
*******************************************************/
import com.shanebow.dao.DataField;
import com.shanebow.dao.DataFieldException;
import com.apo.contact.Dispo;
import com.apo.net.Access;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.*;

public final class OrderStatus
	implements DataField, Comparable<OrderStatus>
	{
	private static final int SEC_PER_DAY = 24*60*60;

	private static final ArrayList<OrderStatus> _all = new ArrayList<OrderStatus>();
	public static final int IDXX =  0;
	public static final int IDWRITTEN  = 1;
	public static final int IDREWRITE  = 2;
	public static final int IDNOVERO   = 30;
	public static final int IDCANREQ   = 35;
	public static final int IDVEROED   = 40;
	public static final int IDDOCSENT  = 50;
	public static final int IDTASENT   = 60;
	public static final int IDDOCWAIT  = 70;
	public static final int IDDOCDUEAO = 73;
	public static final int IDDOCDUERM = 76;
	public static final int IDDOCBACK  = 78;
	public static final int IDCASENT   = 79;
	public static final int IDCAWAIT   = 80;
	public static final int IDCADUEAO  = 83;
	public static final int IDCADUERM  = 86;
	public static final int IDCAIN     = 88;
	public static final int IDPAID     = 90;
	public static final int IDCANCEL   = 100;

	public static final OrderStatus XX = new OrderStatus(
		IDXX, "--",
		0, null, Color.WHITE, IDWRITTEN );

	public static final OrderStatus WRITTEN = new OrderStatus(
		IDWRITTEN, "Written",
		0, Dispo.RMP, Color.YELLOW, IDVEROED, IDNOVERO );

	public static final OrderStatus NOVERO  = new OrderStatus(
		IDNOVERO, "Not Verified",
		0, Dispo.AOP, new Color(250,133,5), IDREWRITE, IDCANREQ );

	public static final OrderStatus REWRITE = new OrderStatus(
		IDREWRITE, "Re-write",
		0, Dispo.RMP, new Color(225,242,14), IDVEROED, IDNOVERO );

	public static final OrderStatus CANREQ  = new OrderStatus(
		IDCANREQ, "Request Cancel",
		0, Dispo.RMP, new Color(242,30,14), IDNOVERO, IDVEROED );

	public static final OrderStatus VEROED  = new OrderStatus(
		IDVEROED, "Verified",
		0, Dispo.ADP, new Color(8,247,11), IDDOCSENT );

	public static final OrderStatus DOCSENT = new OrderStatus(
		IDDOCSENT, "Docs Sent",
		0, Dispo.ADP, new Color(6,177,249), IDTASENT );

	public static final OrderStatus TASENT = new OrderStatus(
		IDTASENT, "TA Sent",
		0, Dispo.AOP, new Color(7,102,248), IDDOCWAIT, IDCANREQ );

	public static final OrderStatus DOCWAIT = new OrderStatus(
		IDDOCWAIT, "Wait for Docs",
		3*SEC_PER_DAY, Dispo.ADP, Color.PINK, IDDOCDUEAO, IDDOCBACK );

	public static final OrderStatus DOCDUEAO = new OrderStatus(
		IDDOCDUEAO, "AO: Docs Overdue",
		0, Dispo.AOP, Color.CYAN, IDDOCWAIT, IDDOCDUERM );

	public static final OrderStatus DOCDUERM = new OrderStatus(
		IDDOCDUERM, "RM: Docs Overdue",
		0, Dispo.RMP, Color.YELLOW, IDDOCWAIT, IDDOCDUEAO );

	public static final OrderStatus DOCBACK = new OrderStatus(
		IDDOCBACK, "Docs Back",
		0, Dispo.ADP, Color.PINK, IDCASENT );

/******** New 4/21/11 ***********/
	public static final OrderStatus CASENT = new OrderStatus(
		IDCASENT, "Banking Sent",
		0, Dispo.AOP, new Color(7,102,248), IDCAWAIT, IDCANREQ );

	public static final OrderStatus CAWAIT = new OrderStatus(
		IDCAWAIT, "Wait for CA",
		2*SEC_PER_DAY, Dispo.ADP, Color.PINK, IDCADUEAO, IDCAIN );

	public static final OrderStatus CADUEAO = new OrderStatus(
		IDCADUEAO, "AO: CA Overdue",
		0, Dispo.AOP, Color.CYAN, IDCAWAIT, IDCADUERM );

	public static final OrderStatus CADUERM = new OrderStatus(
		IDCADUERM, "RM: CA Overdue",
		0, Dispo.RMP, Color.YELLOW, IDCAWAIT, IDCADUEAO );

	public static final OrderStatus CAIN = new OrderStatus(
		IDCAIN, "CA In",
		0, Dispo.ADP, Color.PINK, IDPAID );

// when paid clicked how much & date need to be recorded
	public static final OrderStatus PAID = new OrderStatus(
		IDPAID, "Paid",
		0, Dispo.AOF, Color.MAGENTA );

	public static final OrderStatus CANCEL  = new OrderStatus(
		IDCANCEL,  "Cancel Order",
		0, Dispo.CO, Color.RED );

	public  static final Iterable<OrderStatus> getAll()  { return _all; }

	public static OrderStatus[] interestSet( Dispo aDispo )
		{
		ArrayList<OrderStatus> states = new ArrayList<OrderStatus>();
		for ( OrderStatus os : OrderStatus.getAll())
			if ( aDispo.equals(os.dispo()))
				states.add(os);
		return states.toArray(new OrderStatus[0]);
		}

	public static OrderStatus[] nextStates( Dispo aDispo )
		{
		Set<OrderStatus> states = new TreeSet<OrderStatus>();
		for ( OrderStatus os : OrderStatus.getAll())
			if ( aDispo.equals(os.dispo()))
				for ( int id : os.nextStates())
					{
					OrderStatus bingo = OrderStatus.find(id);
					if ( bingo != OrderStatus.XX ) states.add(bingo);
					}
		int uid = Access.getUID();
		if ( uid == 7 || uid == 2 || uid == 13 || uid == 5)
			states.add(CANCEL);
		return states.toArray(new OrderStatus[0]);
		}

	public static OrderStatus parse( String text )
		throws DataFieldException
		{
		if (text == null)
			return OrderStatus.XX;
		String trimmed = text.trim();
		if ( trimmed.isEmpty() || trimmed.equals("0"))
			return OrderStatus.XX;
		for ( OrderStatus status : _all )
			if ( status.name().equals(trimmed)) return status;
		try
			{
			int id = Integer.parseInt(trimmed);
			return find(id);
			}
		catch(Exception e) {}
		throw new DataFieldException("OrderStatus " + BAD_LOOKUP + text);
		}

	public static OrderStatus read(ResultSet rs, int rsCol)
		throws DataFieldException
		{
		try { return find(rs.getInt(rsCol)); }
		catch (SQLException e) { throw new DataFieldException(e); }
		}

	public static OrderStatus find(int id)
		{
		for ( OrderStatus status : _all )
			if ( status.fID == id )
				return status;
		return OrderStatus.XX;
		}

	public int id() { return fID; }
	public String name() { return fName; }
	public final int callbackDelay() { return fCallbackDelay; }
	public final Color color() { return fColor; }
	public final Dispo dispo() { return fDispo; }
	public final int[] nextStates() { return fNextStates; }
	public final boolean reachableFrom(OrderStatus aOther)
		{
		for ( int id : aOther.fNextStates )
			if ( fID == id ) return true;
// System.out.println( toString() + " NOT REACHABLE from " + aOther );
		return false;
		}

	@Override public boolean equals(Object other) { return this == other; }
	@Override public int compareTo(OrderStatus other) { return this.fID - other.fID; }
	@Override public boolean isEmpty() { return (fID == 0); }
	@Override public String toString() { return fName; }
	@Override public String csvRepresentation() { return (isEmpty()? "" : "" + fID); }
	@Override public String dbRepresentation()  { return "" + fID; }


	// PRIVATE
	private final int fID;
	private final String fName; // the full name
	private final int[] fNextStates;
	private final Dispo fDispo;
	private final Color fColor;
	private final int   fCallbackDelay; // seconds

	private OrderStatus( int id, String name, int aCallbackDelay, Dispo aDispo,
		Color aColor, int... aNextStates )
		{
		fID = id;
		fName = name;
		fCallbackDelay = aCallbackDelay;
		fDispo = aDispo;
		fColor = aColor;
		fNextStates = aNextStates;
		_all.add( this );
		}
	}
