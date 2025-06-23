package oo.com.iseu.UI.Basic;

import java.io.PrintStream;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;

public class StreamToTextPane {
	private  PrintStream pStream = null;
	private  static JTextPane jTextPane;
	private StringBuilder stringBuilder;
	public StreamToTextPane(JTextPane textPane) {
		jTextPane = textPane;
		pStream = new PrintStream(System.out){
		SimpleAttributeSet   attrSet   =   new   SimpleAttributeSet(); 
			@Override
			public synchronized void println(String x) {
				Runnable runnable = new Runnable() {
					public void run() {
						try {
							/*************Insert the content and set the caretPosition to the botton********************/
							jTextPane.getDocument().insertString(jTextPane.getDocument().getLength(), x+"\n", attrSet);
							jTextPane.setCaretPosition(jTextPane.getDocument().getLength());
							jTextPane.repaint();
						} catch (BadLocationException e) {
							return ;
//							e.printStackTrace();
						}
					}
				};
				SwingUtilities.invokeLater(runnable);
			}	
		};
	}
	
	public PrintStream getPStream() {
		return pStream;
	}
	
	public static void cleanUp() {
		jTextPane.setText("");
	}
}
