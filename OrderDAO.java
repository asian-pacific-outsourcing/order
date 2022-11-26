package com.apo.order;
/********************************************************************
* @(#)OrderDAO.java 1.00 20110208
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* OrderDAO: An interface that defines IO methods for Order contact information.
* Concrete implementations should be written to access data directly from
* the database, over the network, or via a file.
*
* @author Rick Salamone
* @version 1.00, 20110208 rts initial version
* @version 1.01, 20110405 rts methods throw DataFieldException
*******************************************************/
import com.apo.contact.touch.TouchCode;
import com.apo.order.Order;
import com.shanebow.dao.*;
import com.shanebow.util.SBLog;
import java.util.List;

public abstract class OrderDAO
	{
	public abstract When getServerTime();
	public abstract OrderID add(Order order)
		throws DataFieldException;
	public abstract void delete(Order order)
		throws DataFieldException;
//	public abstract boolean activity(Order aOrder )
//		throws DataFieldException;
	public abstract void 	update( Order aOrder, When aTime, EmpID aEmpID,
		TouchCode aTouchCode, Comment aComment )
		throws DataFieldException;
	public abstract boolean fetch(List<Order> aList, int aMaxRecords, String aQuery);
	public abstract boolean fetch(List<Order> aList, ContactID aID);
	public abstract boolean supportsDelete();

	// Logging support
	private final String MODULE = getClass().getSimpleName();
	private static final String SEPARATOR="==================================================";
	protected String lastError = "";

	public final String getLastError() { return lastError; }

	protected final void log( String fmt, Object... args )
		{
		SBLog.write( MODULE, String.format(fmt, args));
		}

	protected final void logSeparate( String msg )
		{
		SBLog.write( SEPARATOR );
		SBLog.write( MODULE, msg );
		}

	protected final boolean logError( String msg )
		{
		java.awt.Toolkit.getDefaultToolkit().beep();
		lastError = msg;
		SBLog.error(MODULE + " ERROR", msg );
		return false;
		}

	protected final boolean logSuccess()
		{
		lastError = "";
		SBLog.write(MODULE, "Success" );
		return true;
		}
	}
