package org.jgpd.io.jbpm.definition;

import java.io.*;
import java.util.*;

/**
 * specifies read, write and required properties for fields.
 */
public class FieldAccess implements Serializable {

  private static List accessesById = new ArrayList();
  private static Map accessesByText = new HashMap();

  public static final FieldAccess NOT_ACCESSIBLE = new FieldAccess( "not-accessible" );
  public static final FieldAccess READ_ONLY = new FieldAccess( "read-only" );
  public static final FieldAccess WRITE_ONLY = new FieldAccess( "write-only" );
  public static final FieldAccess WRITE_ONLY_REQUIRED = new FieldAccess( "write-only-required" );
  public static final FieldAccess READ_WRITE = new FieldAccess( "read-write" );
  public static final FieldAccess READ_WRITE_REQUIRED = new FieldAccess( "read-write-required" );

  private FieldAccess( String text ) {
    this.id = accessesById.size();
    this.text = text;
    accessesById.add( id, this );
    accessesByText.put( text, this );
  }

  public String writeXML()
  {
	return toString();
  }

  public static final FieldAccess fromInt( int id ) {
    return (FieldAccess) accessesById.get( id );
  }

  public static final FieldAccess fromText( String text ) {
    return (FieldAccess) accessesByText.get( text );
  }

  public int toInt() {
    return id;
  }

  public String toString() {
    return text;
  }

  public boolean isReadable() {
    return ( ( this == FieldAccess.READ_ONLY )
             || ( this == FieldAccess.READ_WRITE )
             || ( this == FieldAccess.READ_WRITE_REQUIRED ) );
  }

  public boolean isWritable() {
    return ( ( this == FieldAccess.WRITE_ONLY )
             || ( this == FieldAccess.READ_WRITE )
             || ( this == FieldAccess.WRITE_ONLY_REQUIRED )
             || ( this == FieldAccess.READ_WRITE_REQUIRED ) );
  }

  public boolean isRequired() {
    return ( ( this == FieldAccess.WRITE_ONLY_REQUIRED )
             || ( this == FieldAccess.READ_WRITE_REQUIRED ) );
  }

  private int id = -1;
  private String text = null;
}
