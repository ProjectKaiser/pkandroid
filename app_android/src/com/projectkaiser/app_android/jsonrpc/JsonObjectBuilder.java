package com.projectkaiser.app_android.jsonrpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Stack;

public class JsonObjectBuilder {
	
	enum ContainerType {
		OBJECT, ARRAY
	}
	
	class Container {
		ContainerType type;
		int indent;
		int size;
		public Container(ContainerType type, Container parent) {
			this.type = type;
			this.indent = parent==null?1:parent.indent+1;
		}
	}
	
	Stack<Container> m_stack = new Stack<JsonObjectBuilder.Container>();
	
	boolean m_format;
	
	PrintWriter pw;
	
	ByteArrayOutputStream m_buffer;
	
	public JsonObjectBuilder(boolean format) {
		m_format = format;
		try {
			m_buffer = new ByteArrayOutputStream();
			pw = new PrintWriter(new OutputStreamWriter(m_buffer, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void reset() {
		m_stack.clear();
	}
	
	private void addingItem() {
		if (m_stack.size()>0) {
			Container c = m_stack.peek();
			if (c.size>0) pw.append(",");
			if (m_format)
				pw.append("\n");			
			indent();
		}
	}

	private void addedItem() {
		if (m_stack.size()>0) 
			m_stack.peek().size++;
	}
	
	private Container currentContainer() {
		return m_stack.size()>0?m_stack.peek():null;
	}
	
	public void openObject() {
		addingItem();
		pw.append("{");
		m_stack.push(new Container(ContainerType.OBJECT, currentContainer()));
	}
	
	public void closeObject() {
		Container c = m_stack.pop();
		if (c.type != ContainerType.OBJECT)
			throw new RuntimeException("JSON build failed");
		if (m_format)
			pw.append("\n");
		indent();
		pw.append("}");
		addedItem();
	}
	
	private static String hex(char ch) {
        return Integer.toHexString(ch);
    }

	public void addEscapedJson(JsonObjectBuilder json) { 
		
		if (json == null) {
			addNull();
		} else {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			addingItem();
			out.write('\"');

	        for (byte b : json.toByteArray()) {
	            switch (b) {
		          case '"':
		              out.write('\\');
		              out.write('"');
		              break;
		          case '/':
		              out.write('\\');
		              out.write('/');
		              break;
		          case '\\':
		              out.write('\\');
		              out.write('\\');
		              break;
		          default :
		              out.write(b);
		              break;
		      }
			}

	        out.write('\"');

	        try {  
	    		pw.flush();
	        	m_buffer.write(out.toByteArray());
	        } catch (IOException e) {
	        	throw new RuntimeException(e);
	        }
	        
	        addedItem();			
		}
		
	}
	
	private void addEscaped(String str, boolean quote) {
		StringBuilder out = new StringBuilder();
        int sz;
        sz = str.length();
        
        if (quote) out.append("\"");
		
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);

            if (ch < 32) {
                switch (ch) {
                    case '\b':
                        out.append('\\');
                        out.append('b');
                        break;
                    case '\n':
                        out.append('\\');
                        out.append('n');
                        break;
                    case '\t':
                        out.append('\\');
                        out.append('t');
                        break;
                    case '\f':
                        out.append('\\');
                        out.append('f');
                        break;
                    case '\r':
                        out.append('\\');
                        out.append('r');
                        break;
                    default :
                        if (ch > 0xf) {
                            out.append("\\u00" + hex(ch));
                        } else {
                            out.append("\\u000" + hex(ch));
                        }
                        break;
                }
            } else {
                switch (ch) {
//                    case '\'':
//                        if (escapeSingleQuote) out.append('\\');
//                        out.append('\'');
//                        break;
                    case '"':
                        out.append('\\');
                        out.append('"');
                        break;
                    case '/':
                        out.append('\\');
                        out.append('/');
                        break;
                    case '\\':
                        out.append('\\');
                        out.append('\\');
                        break;
                    default :
                        out.append(ch);
                        break;
                }
            }
        }
        if (quote) out.append("\"");
        pw.append(out.toString());
	}

	private void indent() {
		Container c = currentContainer();
		if (c!=null && m_format)
			for (int i=0; i<c.indent; i++) {pw.append("\t");}
	}
	
	public void openArrayProp(String name) {
		addingItem();

		addEscaped(name, true);
		pw.append(":[");

		m_stack.push(new Container(ContainerType.ARRAY, currentContainer()));
	}

	public void openArray() {
		addingItem();

		pw.append("[");

		m_stack.push(new Container(ContainerType.ARRAY, currentContainer()));
	}

	public void closeArrayProp() {
		Container c = m_stack.pop();
		if (c.type != ContainerType.ARRAY)
			throw new RuntimeException("JSON build failed");
		if (m_format) pw.append("\n");
		indent();
		pw.append("]");
		addedItem();
	}
	
	
	public void openObjectProp(String name) {
		addingItem();
		addEscaped(name, true);
		pw.append(":{");
		m_stack.push(new Container(ContainerType.OBJECT, currentContainer()));
	}

	public void closeObjectProp() {
		if (m_format) pw.append("\n");

		Container c = m_stack.pop();
		if (c.type != ContainerType.OBJECT)
			throw new RuntimeException("JSON build failed");
		
		indent();
		pw.append("}");
	}

	public void addProp(String name, String value) {
		addingItem();
		addEscaped(name, true);
		pw.append(":");
		if (null == value) 
			pw.append("null");
		else 
			addEscaped(value, true);
		addedItem();
	}

	public void addProp(String name, Integer value) {
		addingItem();
		addEscaped(name, true);
		pw.append(":");
		if (null == value) 
			pw.append("null");
		else 
			pw.append(String.valueOf(value));
		addedItem();
	}

	public void addProp(String name, Long value) {
		addingItem();
		addEscaped(name, true);
		pw.append(":");
		if (null == value)
			pw.append("null");
		else
			pw.append(value.toString());
		addedItem();
	}
	
	public void throwNonSerializable() {
		throw new RuntimeException("Unsupported param type for serialization");
	}
	
	public void addProp(String name, Boolean value) {
		addingItem();
		addEscaped(name, true);
		pw.append(":");
		if (value == null)
			pw.append("null");
		else
			pw.append(value.booleanValue()?"true":"false");
		addedItem();
	}
	
	public void addString(String value) {
		addingItem();
		if (value == null)
			addNull();
		else {
			addEscaped(value, true);
		}
		addedItem();
	}
	
	public void addNull() {
		addingItem();
		pw.append("null");
		addedItem();
	}
	
	public byte[] toByteArray() {
		pw.flush();
		try {
			m_buffer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
				
		return m_buffer.toByteArray();
	}
	
}
