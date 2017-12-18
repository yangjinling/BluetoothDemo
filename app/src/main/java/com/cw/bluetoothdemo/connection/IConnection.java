package com.cw.bluetoothdemo.connection;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by yangjinling on 2017/11/28.
 */
public interface IConnection {

	public InputStream getInputStream();

	public OutputStream getOutputStream();

}
