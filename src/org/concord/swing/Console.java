/*
 *  Copyright (C) 2004  The Concord Consortium, Inc.,
 *  10 Concord Crossing, Concord, MA 01742
 *
 *  Web Site: http://www.concord.org
 *  Email: info@concord.org
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * END LICENSE */

package org.concord.swing;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Console
extends JTextArea
{
    protected static final WindowAdapter closer = new WindowAdapter()
    {
        public void windowClosing(WindowEvent event)
        {
            JFrame frame = (JFrame) event.getSource();
            frame.dispose();
        }
    };

	boolean running = false;

    protected Vector listeners = new Vector();
	protected Vector streamThreads = new Vector();
    protected Process process;
    protected Thread outputProcess;
    protected Thread errorProcess;
    protected Thread monitorProcess;

    public Console()
    {
        this(null);
    }
    
    public Console(Process process)
    {
        setProcess(process);
    }
    
    public void setProcess(Process newProcess)
    {
        if (process instanceof Process)
        {
            process.destroy();
            try
            {
                process.waitFor();
            }
            catch (InterruptedException e)
            {
            }
        }
        
        process = newProcess;
        
        if (process == null)
            return;
            
        setText("");
		
		streamThreads.removeAllElements();
		
		addInputStream(process.getInputStream(), "stdout");
		addInputStream(process.getErrorStream(), "stderr");

        monitorProcess = new Thread()
        {
            public void run()
            {
                try
                {
                    if (process instanceof Process)
                        process.waitFor();

					running = false;
                    process = null;
                    if (listeners.size() > 0)
                    {
                        ChangeEvent changeEvent = new ChangeEvent(Console.this);
                        for (int i = 0; i < listeners.size(); i++)
                        {
                            ChangeListener listener = (ChangeListener) listeners.elementAt(i);
                            listener.stateChanged(changeEvent);
                        }
                    }
                }
                catch (InterruptedException e)
                {
                }
            }
        };

		start();

        monitorProcess.setPriority(Thread.MIN_PRIORITY);
        monitorProcess.start();
    }

	public void start()
	{
		running = true;
		for(int i=0; i< streamThreads.size(); i++)
		{
			Thread thread = (Thread)streamThreads.elementAt(i);
			thread.start();
		}
	}

	public void addInputStream(InputStream stream, String name)
	{
		Thread thread = new StreamThread(stream, name);
		streamThreads.addElement(thread);
	}

	class StreamThread 
		extends Thread
	{
		InputStream stream;
		String name;

		StreamThread(InputStream stream, String name)
		{
			super(name);
			this.stream = stream;
			this.name = name;
		}

		public void run()
		{
			byte [] buffer = new byte[1024];
			while (running)
			{
				try
				{
					for (int n = stream.read(buffer); n > 0; n = stream.read(buffer))
					{
						String strDebug = new String(buffer, 0, n);
						if(System.getProperty("os.name").equals("Mac OS")){
							strDebug = strDebug.replace('\r','\n');
						}
						Console.this.append(strDebug);
					}
				}
				catch (Exception e)
				{
					Console.this.append(e.toString());
				}
			}
		}
	}

	public PrintStream getPrintStream()
	{
		return new PrintStream(new ConsoleOutputStream(), true);
	}

	class ConsoleOutputStream
		extends OutputStream
	{
		byte[] bytes = new byte [1];
		public void write(int b)
		{
			bytes[0] = (byte)b;
			append(new String(bytes));
		}

		public void write(byte[] b)
		{
			append(new String(b));
		}

		public void write(byte[] b, int off, int len)
		{
			append(new String(b, off, len));
		} 
	}


    public Process getProcess()
    {
        return process;
    }
    
    public void addChangeListener(ChangeListener listener)
    {
        listeners.addElement(listener);
    }
    
    public void removeChangeListener(ChangeListener listener)
    {
        listeners.removeElement(listener);
    }
}

