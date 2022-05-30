/* ************************************************************************* *
 *                                                                           *
 *        Copyright (c) 2004 Peter Cappello  <cappello@cs.ucsb.edu>          *
 *                                                                           *
 *    Permission is hereby granted, free of charge, to any person obtaining  *
 *  a copy of this software and associated documentation files (the          *
 *  "Software"), to deal in the Software without restriction, including      *
 *  without limitation the rights to use, copy, modify, merge, publish,      *
 *  distribute, sublicense, and/or sell copies of the Software, and to       *
 *  permit persons to whom the Software is furnished to do so, subject to    *
 *  the following conditions:                                                *
 *                                                                           *
 *    The above copyright notice and this permission notice shall be         *
 *  included in all copies or substantial portions of the Software.          *
 *                                                                           *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,        *
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF       *
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.   *
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY     *
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,     *
 *  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE        *
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                   *
 *                                                                           *
 * ************************************************************************* */

package edu.ucsb.cs.jicos.services.external.services;

import edu.ucsb.cs.jicos.services.Shared;
import edu.ucsb.cs.jicos.services.Task;
import edu.ucsb.cs.jicos.services.external.ExternalData;
import edu.ucsb.cs.jicos.services.external.XmlConverter;
import edu.ucsb.cs.jicos.services.external.XmlDocument;

/**
 * 
 * @author Pete Cappello
 */
public class TaskExternal extends Task implements XmlConverter
{

    //-- Variables -----------------------------------------------------------

    // key of Map to reference to TaskServer that handles associated hosts
    protected Class serviceClass;

    //-- Constructors --------------------------------------------------------

    protected TaskExternal()
    {
        this.serviceClass = this.getClass();
    }

    protected TaskExternal(Class serviceClass)
    {
        this.serviceClass = serviceClass;
    }

    //-- Methods --------------------------------------------------------

    public Class serviceClass()
    {
        return serviceClass;
    }

    public Object execute( edu.ucsb.cs.jicos.services.Environment environment )
    {
        return null;
    }
    
    
    //-- External functions --------------------------------------------------

    public String toXml(String prefix) throws Exception {
		return ("");
	}

	public boolean fromXml(ExternalData externalData) throws Exception {
		return (false);
	}

	public Object createInput(ExternalData externalData) throws Exception {
		return ((Object) null);
	}

	public Shared createShared(ExternalData externalData) throws Exception {
		return ((Shared) null);
	}

	public XmlDocument createResult(Object result) throws Exception {
		return ((XmlDocument) null);
	}
	
	public org.w3c.dom.Document getStyleSheet(int styleSheetType) {
		org.w3c.dom.Document xsltStyleSheet = null;
		
		switch (styleSheetType) {
		case XmlConverter.STYLESHEET_Unknown:
		case XmlConverter.STYLESHEET_Xml:
		case XmlConverter.STYLESHEET_Html:
			break;
		}
		
		return ((org.w3c.dom.Document) null);
	}

	public String toHtmlString(XmlDocument result, String hostPort) {
		return ((String) null);
	}

}