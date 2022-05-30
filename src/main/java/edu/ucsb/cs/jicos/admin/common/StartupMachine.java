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

/**
 * A machine to be used in the launch config.  This is used pretty much as a
 * container for keeping a bunch of values together.  As a result, all
 * member variables are public.
 * 
 * There will be some confusion, as this class is used to identify to a
 * particular computer instance, as well as refer to a location where to start
 * a service.
 *
 * @author  pippin
 */

package edu.ucsb.cs.jicos.admin.common;

import edu.ucsb.cs.jicos.services.external.XmlDocument;

import java.io.Serializable;

import org.w3c.dom.Element;

public class StartupMachine implements Serializable
{
    //
    //-- Constants -----------------------------------------------------------

    public static final int STARTSTYLE_unknown = 0;
    public static final int STARTSTYLE_Cham = 1;
    public static final int STARTSTYLE_SSh = 2;

    public static final String DEFAULT_STARTSTYLE = "ssh";

    //
    //-- Variables -----------------------------------------------------------

    /**  The domain of a service.  */
    public String domain;
    
    /**  The machine on which to start a service.  */
    public String machine;
    
    /**  The group on which to start a service.  */
    public String group;
    
    /**  How many hosts to start up with the service.  */
    public String hosts;
    
    /**  The external type of service.  */
    public int  extType;
    
    /**  The JVM options for the service.  */
    public String jvmOptions;

    /**  Status of service start.  */
    public boolean started;

    /**  Status of service start.  */
    public int startStyle;

    //
    //-- Constructors --------------------------------------------------------
    
    public StartupMachine()
    {
        this.initVariables();
    };

    public StartupMachine(XmlDocument xml, StartupConfig.StartOptions startOptions )
    {
        this.initVariables();

        // Assign defaults.
        if( null != startOptions ) {
            startStyle = startOptions.startStyle;
        }

        // Process the XML.
        this.domain = xml.getValue( "@domain" );
        this.machine = xml.getValue( "@machine" );
        this.group = xml.getValue( "@group" );
        this.hosts = xml.getValue( "@hosts" );
        
        String strExtType = xml.getValue( "@exttype" );
        if( null != strExtType )
        {
            if( strExtType.equalsIgnoreCase( "matlab" ) )
            {
                this.extType = Launcher.EXTTYPE_Matlab;
            }
        }

        String strStartStyle = xml.getValue( "@startstyle" );
        if( null != strStartStyle ) {
            startStyle = getStartStyle( strStartStyle );
        }

        return;
    }

    public StartupMachine(Element xmlElement)
    {
        this( new XmlDocument( xmlElement ), null );
    }

    private void initVariables()
    {
        this.domain = null;
        this.machine = null;
        this.group = null;
        this.hosts = null;
        this.extType = Launcher.EXTTYPE_None;
        this.jvmOptions = null;
        
        this.started = false;
        this.startStyle = STARTSTYLE_unknown;

        return;
    }


    //
    //-- Accessors -----------------------------------------------------------

    public String toString()
    {
        StringBuffer stringBuffer = new StringBuffer( "StartupMachine" );

        stringBuffer.append( "[machine=" );
        if (null != machine)
        {
            stringBuffer.append( machine );
        }

        stringBuffer.append( ",domain=" );
        if (null != domain)
        {
            stringBuffer.append( domain );
        }

        stringBuffer.append( ",group=" );
        if (null != group)
        {
            stringBuffer.append( group );
        }

        stringBuffer.append( ",started=" + this.started );

        switch (this.extType)
        {
            case Launcher.EXTTYPE_None:
                stringBuffer.append( ",extType=none" );
                break;
            case Launcher.EXTTYPE_Matlab:
                stringBuffer.append( ",extType=matlab" );
                break;
        }

        stringBuffer.append( ",jvmOptions=" );
        if (null != jvmOptions)
        {
            stringBuffer.append( jvmOptions );
        }

        stringBuffer.append( ",startStyle=" );
        stringBuffer.append( getStartStyle( startStyle ) );

        stringBuffer.append( "]" );
        return (stringBuffer.toString());
    }


    /**
     * Get the code for the starting style.  Known values are: "ssh" and "chameleon".
     *
     * @param styleName  Name of the starting style.
     * @return Code of style, or STARTSTYLE_unknown on error.
     */
    public static int getStartStyle( String styleName ) {
        int style = STARTSTYLE_unknown;

        if( null == styleName ) {
            // nop
        } else if( styleName.equalsIgnoreCase( "ssh" ) ) {
            style = STARTSTYLE_SSh;
        } else if( styleName.toLowerCase().startsWith( "cham" ) ) {
            style = STARTSTYLE_Cham;
        }

        return( style );
    }

    public static String getStartStyle( int code ) {
        String startStyle = "unknown";

        switch( code ) {
            case STARTSTYLE_SSh:
                startStyle = "ssh";
                break;
            case  STARTSTYLE_Cham:
                startStyle = "chameleon";
                break;
        }

        return( startStyle );
    }

}