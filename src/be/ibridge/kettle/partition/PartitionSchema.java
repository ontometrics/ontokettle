package be.ibridge.kettle.partition;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.ChangedFlag;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SharedObjectInterface;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.repository.Repository;

/**
 * A partition schema allow you to partition a step according into a number of partitions that run independendly.
 * It allows us to "map" 
 * 
 * @author Matt
 *
 */
public class PartitionSchema extends ChangedFlag implements Cloneable, SharedObjectInterface
{
    public static final String XML_TAG = "partitionschema";

    private String   name;

    private String[] partitionIDs;
    private boolean shared;

    public PartitionSchema()
    {
        partitionIDs=new String[] {};
    }
    
    /**
     * @param name
     * @param partitionIDs
     */
    public PartitionSchema(String name, String[] partitionIDs)
    {
        this.name = name;
        this.partitionIDs = partitionIDs;
    }

    public Object clone()
    {
        String[] ids = new String[partitionIDs.length];
        for (int i=0;i<ids.length;i++) ids[i] = partitionIDs[i];
        
        return new PartitionSchema(name, ids);
    }
    
    public String toString()
    {
        return name;
    }
    
    public boolean equals(Object obj)
    {
        if (obj==null) return false;
        return name.equals(((PartitionSchema)obj).name);
    }

    public int hashCode()
    {
        return name.hashCode();
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the partitionIDs
     */
    public String[] getPartitionIDs()
    {
        return partitionIDs;
    }

    /**
     * @param partitionIDs the partitionIDs to set
     */
    public void setPartitionIDs(String[] partitionIDs)
    {
        this.partitionIDs = partitionIDs;
    }

    public String getXML()
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("        <"+XML_TAG+">"+Const.CR);
        xml.append("          "+XMLHandler.addTagValue("name", name));
        for (int i=0;i<partitionIDs.length;i++)
        {
            xml.append("          <partition>");
            xml.append("            "+XMLHandler.addTagValue("id", partitionIDs[i]));
            xml.append("            </partition>");
        }
        xml.append("          </"+XML_TAG+">"+Const.CR);
        return xml.toString();
    }
    
    public PartitionSchema(Node partitionSchemaNode)
    {
        name = XMLHandler.getTagValue(partitionSchemaNode, "name");
        
        int nrIDs = XMLHandler.countNodes(partitionSchemaNode, "partition");
        partitionIDs = new String[nrIDs];
        for (int i=0;i<nrIDs;i++)
        {
            Node partitionNode = XMLHandler.getSubNodeByNr(partitionSchemaNode, "partition", i);
            partitionIDs[i] = XMLHandler.getTagValue(partitionNode, "id");
        }
    }
    
    public void saveRep(Repository rep, long id_transformation) throws KettleDatabaseException
    {
        long id_partition_schema = rep.insertPartitionSchema(id_transformation, name);
        
        for (int i=0;i<partitionIDs.length;i++)
        {
            rep.insertPartition(id_transformation, id_partition_schema, partitionIDs[i]);
        }
    }
    
    public PartitionSchema(Repository rep, long id_partition_schema) throws KettleDatabaseException
    {
        Row row = rep.getPartitionSchema(id_partition_schema);
        
        name = row.getString("SCHEMA_NAME", null);
        
        long[] pids = rep.getPartitionIDs(id_partition_schema);
        partitionIDs = new String[pids.length];
        for (int i=0;i<pids.length;i++)
        {
            partitionIDs[i] = rep.getPartition(pids[i]).getString("PARTITION_ID", null);
        }
    }

    /**
     * @return the shared
     */
    public boolean isShared()
    {
        return shared;
    }

    /**
     * @param shared the shared to set
     */
    public void setShared(boolean shared)
    {
        this.shared = shared;
    }

}