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
 *  Provide methods to load a property file (sometimes refered to as a 
 *  "resource bundle").
 *
 *  Created on Nov 12, 2004
 *
 * @author  Andy Pippin
 */

package edu.ucsb.cs.jicos.services;

import  edu.ucsb.cs.jicos.foundation.Command;

import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;

public final class Property 
{
	/**
	 * Helper method.
	 * 
	 * @param filename  Name of the properties file.
	 */
	public static void load( String filename ) 
    {
		if( null != filename ) 
        {
			load( new File( filename ) );
		}
	}	
	
	/**
	 *  Load the properties from the given file.
	 *
	 * @param   file   Property file.
	 */
	public static void load( File file ) 
    {
		FileInputStream inputStream = null;

		try 
        {
			Properties properties;

			if ( (null != file)
				  && (file.exists())
				  && (null != (inputStream = new java.io.FileInputStream(file)))
				  && (null != (properties = new Properties()))) 
            {
				properties.load(inputStream);
				java.util.Enumeration enumeration = properties.propertyNames();

				while (enumeration.hasMoreElements()) 
                {
					String key = (String) enumeration.nextElement();
					if (null != key) 
                    {
						String value = (String) properties.getProperty(key);
						System.setProperty(key, makeSubstitutions( value ));
					}
				}
			}
		} 
        catch (Exception exception) 
        {
			try 
            {
				System.out.flush();
				System.err.println("ERROR: Couldn't load property file: \""
						+ file.getCanonicalPath() + "\"");
				System.err.println("    " + exception.getMessage());
			} 
            catch (java.io.IOException ignored) {}
		}
		finally 
        {
			if (null != inputStream) 
            {
				try 
                {
					inputStream.close();
				} 
                catch (Exception ignore) {}
			}
		}
		return;
	}

	
	/**
	 *  Identify the root of the package.  Useful for getting file out of a JAR file.
	 * 
	 * @return  The Uniform Resource Identifier of the top of the class tree.
	 */
	public static URI getTopDirectory() 
    {
		URI topDir = null;
		URL getURL = null;

		if (null != (getURL = Property.class.getResource("Property.class"))) 
        {
			String stringTopDir = getURL.toString().replaceFirst("/edu/ucsb/cs/jicos/services/Property.class$",
					"");

			try 
            {
				topDir = new URI(stringTopDir);
			} 
            catch (java.net.URISyntaxException uriSyntaxException) 
            {
				System.out.flush();
				uriSyntaxException.printStackTrace( System.err );
				System.err.flush();
			}
		}
		return (topDir);
	}
	
	/**
	 *  Substitute variables of the form "${property}" with ther values.
	 *  If the value is not defined at the time, then the substitution 
	 *  string will be "".
	 * 
	 * @param value  The value to substitute for.
	 * @return  The new value.
	 */
	private static String makeSubstitutions( String value ) 
    {
		String result = null;
		int ndx = 0;
		int lastNdx = 0;
		int rparen = 0;
		
		try 
        {
			if( null != value ) 
            {
				result = new String();
			    while( -1 != (ndx = value.indexOf( "${", lastNdx )) ) 
                {
                    if( -1 == (rparen = value.indexOf( "}", ndx+2 )) ) 
                    {
                        break; // We're done if no }.
                    } 
                    else 
                    {
                        result += value.substring( lastNdx, ndx );
                        lastNdx = rparen+1;
                        //
                        String varName = value.substring( ndx+2, rparen );
                        result += System.getProperty( varName, "" );
                    }
                }
			}
			
			// Tack on the rest.
			result += value.substring( lastNdx );
			
		} 
        catch ( Exception anyException ) 
        {
			result = null;
		}		
		return( result );
	}
	
	
	/** Load all the properties for this, and all parent classes.
	 * 
	 * @param cmdLine
	 */
	public static void load( Class loadClass ) 
    {		
		if( null != loadClass ) 
        {
			// Get the path to the class.
			String baseClass = Command.class.getName().replace( '.', '\u2345' );
			baseClass = baseClass.replaceAll( "jicos\u2345foundation\u2345Command$", "" );
			// Probably: edu.ucsb.cs.
			
			String propertyName = loadClass.getName().replace( '.', '\u2345' );
			propertyName = propertyName.replaceAll( "^"+baseClass, "" );
			propertyName = propertyName.replace( '\u2345', '.') + ".config";
			// Probably: jicos.some.class.name.config
			
			String propertyValue = System.getProperty( propertyName );
			if( null != propertyValue ) 
            {
				load( new File( propertyValue ) );
				load( new File( propertyValue.toLowerCase() ) );
			}
		}
	}
	
	
	
	/**
	 * Load the default (first) then this user's (after) properties.
	 * 
	 * @param  cmdLine  Command line arguments.
	 */
	public static void loadProperties(String[] cmdLine) 
    {
		String dirName = null;
		java.io.File propertyFile = null;

		// Get the properties from the System, and save any Jicos properties.
		// Since it is unlikely that there are any Jicos properties defined in
		// the JVM property files, anything that is set here should have come
		// from the command line of the JVM.
		//
		// These should override any other value.
		//
		java.util.Properties jicosProperties = new java.util.Properties();
		java.util.Properties systemProperties = System.getProperties();
		java.util.Enumeration enumeration = systemProperties.propertyNames();

		while (enumeration.hasMoreElements()) 
        {
			// Extract all the jicos variables from the System's properties.
			String key = (String) enumeration.nextElement();
			if ( (null != key) && (key.startsWith("jicos.")) ) 
            {
				String value = (String) systemProperties.getProperty(key);
				jicosProperties.setProperty(key, value);
			}
		}		
		
		// First thing to set is the root of the class path.
		java.net.URI topDir = getTopDirectory();
		if( null == jicosProperties.getProperty( "jicos.home" ) ) 
        {
			System.setProperty( "jicos.home", topDir.toString() );
		}
		
		// Check the command line for additional definitions.  These should
		// also override any value in the configuration files.
		//
		for ( int argc = 0; argc < cmdLine.length; ++argc ) 
        {
			if (cmdLine[argc].startsWith("-D")) 
            {
				String[] keyValue = cmdLine[argc].substring(2).split("=");
				if ( 1 == keyValue.length ) 
                {
					System.setProperty(keyValue[0], "true");
					jicosProperties.setProperty( keyValue[0], "true" );
				} 
                else 
                {
					System.setProperty(keyValue[0], makeSubstitutions( keyValue[1] ));
					jicosProperties.setProperty(keyValue[0], keyValue[1]);
				}
			}
		}
		
		// Load the default properties.
		String globalProperties = topDir.getPath() + "/default.properties";
		propertyFile = new java.io.File(globalProperties);
		load( propertyFile );

		
		// Get any user-defined property files.  These will be located in the
		// home directory.  If this is a windows box, the properties are in:
		//     "${user.home}\Application Data\Jicos\jicos.properties"
		// Otherwise (in the *nix world), the file will be in:
		//     "${user.home}/.jicos/properties"
		//
		dirName = System.getProperty("user.home");
		if (System.getProperty("os.name").toLowerCase().startsWith("window")) 
        {
			dirName += java.io.File.separator + "Application Data"
					+ java.io.File.separator + "Jicos"
					+ java.io.File.separator;
		} 
        else 
        {
			dirName += java.io.File.separator + ".jicos" + java.io.File.separator;
		}

		propertyFile = new java.io.File(dirName + "properties");
		load(propertyFile);

		// Put back in the properties from the JVM command line.
		enumeration = jicosProperties.propertyNames();
		while (enumeration.hasMoreElements()) 
        {
			String key = (String) enumeration.nextElement();
			String value = (String) jicosProperties.getProperty(key);
			System.setProperty(key, value);
		}
		return;
	}
}