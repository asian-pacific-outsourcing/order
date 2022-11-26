package com.apo.order;
/********************************************************************
* @(#)OrderTablePopup.java 1.00 20110404
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* OrderTablePopup: An interface to specify a pop up dialog to the OrderTable.
*
* @author Rick Salamone
* @version 1.00, 20110404 rts created
*******************************************************/
public interface OrderTablePopup
	{
	public void setOrder(Order aOrder);
	public void setVisible(boolean aVisible);
	}