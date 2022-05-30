package org.jgpd.io.jbpm.definition.impl;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import org.jgpd.io.jbpm.xml.*;

public class DefinitionComponentImpl {

  private DefinitionComponentImpl() {
  }

  /**
   * gets the singleton instance.
   */
  public static DefinitionComponentImpl getInstance() {
    return instance;
  }

  private static final String queryFindProcessDefinitions =
    "select pd " +
    "from pd in class org.jbpm.workflow.definition.impl.ProcessDefinitionImpl " +
    "where pd.version = ( " +
    "    select max(pd2.version) " +
    "    from pd2 in class org.jbpm.workflow.definition.impl.ProcessDefinitionImpl " +
    "    where pd2.name = pd.name )";

  public Collection getProcessDefinitions() {
    return null;
  }

  private static final String queryFindProcessDefinitionByName =
    "select pd " +
    "from pd in class org.jbpm.workflow.definition.impl.ProcessDefinitionImpl " +
    "where pd.name = ? " +
    "  and pd.version = ( " +
    "    select max(pd2.version) " +
    "    from pd2 in class org.jbpm.workflow.definition.impl.ProcessDefinitionImpl " +
    "    where pd2.name = pd.name )";

  private static final String queryFindAllProcessDefinitions =
    "from pd in class org.jbpm.workflow.definition.impl.ProcessDefinitionImpl";

  private XmlElement getXmlElementForEntry( String entryName, Map entries ) {
    byte[] processDefinitionXml =  (byte[]) entries.get( entryName );
    if ( processDefinitionXml == null ) {
      throw new RuntimeException( "no '" + entryName + "' found in process archive" );
    }
    InputStream processDefinitionInputStream = new ByteArrayInputStream( processDefinitionXml );
    return XmlParser.parse(processDefinitionInputStream);
  }

  /**
   * reads the jarFile-InputStream and puts all entries in a Map, for which
   * the keys are the path-names.
   * @param jarFile is the JarInputStream for the process definition jar file.
   * @return a Map with the entry-path-names as keys and the byte-arrays as the contents.
   */
  private Map readEntries( JarInputStream processArchiveStream ) throws IOException {
    Map entries = new HashMap();

    JarEntry entry = processArchiveStream.getNextJarEntry();
    while ( entry != null ) {
      byte[] entryContent = this.readFile( processArchiveStream );

      if ( entryContent != null ) {
        entries.put( entry.getName(), entryContent );
      }

      entry = processArchiveStream.getNextJarEntry();
    }

    return entries;
  }

  private byte[] readFile(InputStream in) {
    byte[] fileContents = null;
    int fileSize = 0;

    try {
      byte[] buffer = new byte[BUFFER_SIZE];
      int bytesRead = in.read(buffer);

      while (bytesRead != -1) {

        byte[] newFileContents = new byte[fileSize + bytesRead];

        if (fileSize > 0)
          System.arraycopy(fileContents, 0, newFileContents, 0, fileSize);
        System.arraycopy(buffer, 0, newFileContents, fileSize, bytesRead);

        fileContents = newFileContents;
        fileSize += bytesRead;

        bytesRead = in.read(buffer);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return fileContents;
  }

  private static final int BUFFER_SIZE = 512;
  private static final DefinitionComponentImpl instance = new DefinitionComponentImpl();
}
