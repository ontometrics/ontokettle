 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.job.entry.mysqlbulkfile;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;


/**
 * This defines an SQL job entry.
 * 
 * @author Samatar
 * @since 05-03-2006
 *
 */
public class JobEntryMysqlBulkFile extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String tablename;
	private String schemaname;
	private String filename;
	private String separator;
	private String enclosed;
	private String lineterminated;
	private String limitlines;
	private String listcolumn;
	private boolean highpriority;
	public int outdumpvalue;
	public int iffileexists;

	private DatabaseMeta connection;

	public JobEntryMysqlBulkFile(String n)
	{
		super(n, "");
		tablename=null;
		schemaname=null;
		filename=null;
		separator=null;
		enclosed=null;
		limitlines = "0";
		listcolumn=null;
		lineterminated=null;
		highpriority=true;
		iffileexists=2;
		connection=null;
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_MYSQL_BULK_FILE);
	}

	public JobEntryMysqlBulkFile()
	{
		this("");
	}

	public JobEntryMysqlBulkFile(JobEntryBase jeb)
	{
		super(jeb);
	}
    
	public Object clone()
	{
		JobEntryMysqlBulkFile je = (JobEntryMysqlBulkFile) super.clone();
		return je;
	}

	public String getXML()
	{
		StringBuffer retval = new StringBuffer(200);
		
		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("schemaname",  schemaname));
		retval.append("      ").append(XMLHandler.addTagValue("tablename",  tablename));
		retval.append("      ").append(XMLHandler.addTagValue("filename",  filename));
		retval.append("      ").append(XMLHandler.addTagValue("separator",  separator));
		retval.append("      ").append(XMLHandler.addTagValue("enclosed",  enclosed));
		retval.append("      ").append(XMLHandler.addTagValue("lineterminated",  lineterminated));
		retval.append("      ").append(XMLHandler.addTagValue("limitlines",  limitlines));
		retval.append("      ").append(XMLHandler.addTagValue("listcolumn",  listcolumn));
		retval.append("      ").append(XMLHandler.addTagValue("highpriority",  highpriority));
		retval.append("      ").append(XMLHandler.addTagValue("outdumpvalue",  outdumpvalue));
		retval.append("      ").append(XMLHandler.addTagValue("iffileexists",  iffileexists));
		retval.append("      ").append(XMLHandler.addTagValue("connection", connection==null?null:connection.getName()));
		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			schemaname     = XMLHandler.getTagValue(entrynode, "schemaname");
			tablename     = XMLHandler.getTagValue(entrynode, "tablename");
			filename     = XMLHandler.getTagValue(entrynode, "filename");
			separator     = XMLHandler.getTagValue(entrynode, "separator");
			enclosed     = XMLHandler.getTagValue(entrynode, "enclosed");
			lineterminated     = XMLHandler.getTagValue(entrynode, "lineterminated");
			limitlines     = XMLHandler.getTagValue(entrynode, "limitlines");
			listcolumn     = XMLHandler.getTagValue(entrynode, "listcolumn");
			highpriority = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "highpriority"));	
			outdumpvalue     = Const.toInt(XMLHandler.getTagValue(entrynode, "outdumpvalue"), -1);
			iffileexists = Const.toInt(XMLHandler.getTagValue(entrynode, "iffileexists"), -1);
			String dbname = XMLHandler.getTagValue(entrynode, "connection");
			connection    = DatabaseMeta.findDatabase(databases, dbname);
		}
		catch(KettleException e)
		{
			throw new KettleXMLException("Unable to load job entry of type 'table exists' from XML node", e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			schemaname  = rep.getJobEntryAttributeString(id_jobentry, "schemaname");
			tablename  = rep.getJobEntryAttributeString(id_jobentry, "tablename");
			filename  = rep.getJobEntryAttributeString(id_jobentry, "filename");
			separator  = rep.getJobEntryAttributeString(id_jobentry, "separator");
			enclosed  = rep.getJobEntryAttributeString(id_jobentry, "enclosed");
			lineterminated  = rep.getJobEntryAttributeString(id_jobentry, "lineterminated");		
			limitlines  = rep.getJobEntryAttributeString(id_jobentry, "limitlines");
			listcolumn  = rep.getJobEntryAttributeString(id_jobentry, "listcolumn");
			highpriority=rep.getJobEntryAttributeBoolean(id_jobentry, "highpriority");
			outdumpvalue=Const.toInt(rep.getJobEntryAttributeString(id_jobentry, "outdumpvalue"),-1);
			iffileexists=Const.toInt(rep.getJobEntryAttributeString(id_jobentry, "iffileexists"),-1);
			
			long id_db = rep.getJobEntryAttributeInteger(id_jobentry, "id_database");
			if (id_db>0)
			{
				connection = DatabaseMeta.findDatabase(databases, id_db);
			}
			else
			{
				// This is were we end up in normally, the previous lines are for backward compatibility.
				connection = DatabaseMeta.findDatabase(databases, rep.getJobEntryAttributeString(id_jobentry, "connection"));
			}

		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'table exists' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			rep.saveJobEntryAttribute(id_job, getID(), "schemaname", schemaname);
			rep.saveJobEntryAttribute(id_job, getID(), "tablename", tablename);
			rep.saveJobEntryAttribute(id_job, getID(), "filename", filename);
			rep.saveJobEntryAttribute(id_job, getID(), "separator", separator);
			rep.saveJobEntryAttribute(id_job, getID(), "enclosed", enclosed);
			rep.saveJobEntryAttribute(id_job, getID(), "lineterminated", lineterminated);
			rep.saveJobEntryAttribute(id_job, getID(), "limitlines", limitlines);
			rep.saveJobEntryAttribute(id_job, getID(), "listcolumn", listcolumn);	
			rep.saveJobEntryAttribute(id_job, getID(), "highpriority", highpriority);
			rep.saveJobEntryAttribute(id_job, getID(), "outdumpvalue", outdumpvalue);
			rep.saveJobEntryAttribute(id_job, getID(), "iffileexists", iffileexists);




			if (connection!=null) rep.saveJobEntryAttribute(id_job, getID(), "connection", connection.getName());
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'Mysql Bulk Load' to the repository for id_job="+id_job, dbe);
		}
	}

	
	public void setTablename(String tablename)
	{
		this.tablename = tablename;
	}
	public void setSchemaname(String schemaname)
	{
		this.schemaname = schemaname;
	}
	
	public String getTablename()
	{
		return tablename;
	}
	public String getSchemaname()
	{
		return schemaname;
	}
	
	public void setDatabase(DatabaseMeta database)
	{
		this.connection = database;
	}
	
	public DatabaseMeta getDatabase()
	{
		return connection;
	}
	
	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return false;
	}

	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
	
		String LimitNbrLignes="";
		String ListOfColumn="*";
		String strHighPriority="";
		String OutDumpText="";
		String OptionEnclosed="";
		String FieldSeparator="";
		String LinesTerminated="";

		LogWriter log = LogWriter.getInstance();
		
		Result result = new Result(nr);
		result.setResult(false);

		// Let's check  the filename ...
		if (filename!=null)
		{
			// User has specified a file, We can continue ...
			String realFilename = getRealFilename(); 
			File file = new File(realFilename);

			if (file.exists() && iffileexists==2)
			{
				// the file exists and user want to Fail
				result.setResult( false );
				result.setNrErrors(1);
				log.logError(toString(),Messages.getString("JobMysqlBulkFile.FileExists1.Label") +
					realFilename + Messages.getString("JobMysqlBulkFile.FileExists2.Label"));

			}
			else if (file.exists() && iffileexists==1)
			{
				// the file exists and user want to do nothing
				result.setResult( true );
				log.logDetailed(toString(),Messages.getString("JobMysqlBulkFile.FileExists1.Label") +
						realFilename + Messages.getString("JobMysqlBulkFile.FileExists2.Label"));

			}
			else
			{

				if (file.exists() && iffileexists==0)
				{
					// File exists and user want to renamme it with unique name
		
					//Format Date
		
					DateFormat dateFormat = new SimpleDateFormat("mmddyyyy_hhmmss");
					// Try to clean filename (without wildcard)
					String wildcard = realFilename.substring(realFilename.length()-4,realFilename.length());
					if(wildcard.substring(0,1).equals("."))
					{
						// Find wildcard			
						realFilename=realFilename.substring(0,realFilename.length()-4) + 
							"_" + dateFormat.format(new Date()) + wildcard;
					}
					else
					{
						// did not find wilcard
						realFilename=realFilename + "_" + dateFormat.format(new Date());
					}
					
					log.logDebug(toString(), Messages.getString("JobMysqlBulkFile.FileNameChange1.Label") + realFilename + 
						Messages.getString("JobMysqlBulkFile.FileNameChange1.Label"));



				}

				// User has specified an existing file, We can continue ...
				log.logDetailed(toString(), Messages.getString("JobMysqlBulkFile.FileExists1.Label") +
									realFilename + Messages.getString("JobMysqlBulkFile.FileExists2.Label"));


				if (connection!=null)
				{
					// User has specified a connection, We can continue ...
					Database db = new Database(connection);
					try
					{
						db.connect();
						// Get schemaname
						String realSchemaname = StringUtil.environmentSubstitute(schemaname);
						// Get tablename
						String realTablename = StringUtil.environmentSubstitute(tablename);

						if (db.checkTableExists(realTablename))
						{
							// The table existe, We can continue ...
							log.logDetailed(toString(), "Table ["+realTablename+"] exists.");
							
							// Add schemaname (Most the time Schemaname.Tablename) 
							if (schemaname !=null)
							{
								realTablename= realSchemaname + "." + realTablename;
							}

							// Set the Limit lines
							if (Const.toInt(getRealLimitlines(),0)>0)
							{
								LimitNbrLignes = " LIMIT " + getRealLimitlines() + " " ;
							}

							// Set list of Column, if null get all columns (*) 
							if (getRealListColumn()!= null )
							{
								ListOfColumn= getRealListColumn() ;	
							}
									

							// Fields separator 
							if (getRealSeparator()!= null && outdumpvalue == 0)
							{
								FieldSeparator=" FIELDS TERMINATED BY '" + getRealSeparator() + "' ";
							}

							// Lines Terminated by 
							if (getRealLineterminated()!= null && outdumpvalue == 0)
							{
								LinesTerminated=" LINES TERMINATED BY '" + getRealLineterminated() + "' ";
							}
								


							// High Priority ?
							if (isHighPriority())
							{
								strHighPriority = " HIGH_PRIORITY ";
							}

							if (getRealEnclosed()!= null && outdumpvalue == 0)
							{
								OptionEnclosed=" OPTIONALLY ENCLOSED BY '" + getRealEnclosed() + "' ";
							}

							// OutFile or Dumpfile
							if (outdumpvalue == 0)
							{
								OutDumpText =" INTO OUTFILE ";
							}
							else
							{
								OutDumpText = " INTO DUMPFILE ";
							}

					
							String FILEBulkFile = "SELECT " + strHighPriority + ListOfColumn + OutDumpText + "'" + realFilename	+ "'" + FieldSeparator +
										OptionEnclosed + LinesTerminated  + " FROM " + 	realTablename + LimitNbrLignes + 
										" LOCK IN SHARE MODE";

							try
							{
								// Run the SQL
								PreparedStatement ps= db.prepareSQL(FILEBulkFile);
								ps.execute();

								// Everything is OK...we can deconnect now
								db.disconnect();
								result.setResult(true);

							
							}
							catch(SQLException je)
							{
								db.disconnect();
								result.setNrErrors(1);
								log.logError(toString(), Messages.getString("JobMysqlBulkFile.Error.Label") + " "+je.getMessage());
							}
							


						}
						else
						{
							// Of course, the table should have been created already before the bulk load operation
							db.disconnect();
							result.setNrErrors(1);
							log.logDetailed(toString(), Messages.getString("JobMysqlBulkFile.TableNotExists1.Label") +realTablename+
									Messages.getString("JobMysqlBulkFile.TableNotExists2.Label"));
						}


					}
					catch(KettleDatabaseException dbe)
					{
						db.disconnect();
						result.setNrErrors(1);
						log.logError(toString(), Messages.getString("JobMysqlBulkFile.Error.Label")  + " " + dbe.getMessage());
					}
					
					

				}

				else
				{
					// No database connection is defined
					result.setNrErrors(1);
					log.logError(toString(),  Messages.getString("JobMysqlBulkFile.Nodatabase.Label"));
				}


			}
		

		}
		else
		{
			// No file was specified
			result.setNrErrors(1);
			log.logError(toString(), Messages.getString("JobMysqlBulkFile.Nofilename.Label"));
		}

		return result;

	}

	public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) 
	{
		return new JobEntryMysqlBulkFileDialog(shell,this,jobMeta);
	}
    
	public DatabaseMeta[] getUsedDatabaseConnections()
	{
		return new DatabaseMeta[] { connection, };
	}




	public void setHighPriority(boolean highpriority) 
	{
		this.highpriority = highpriority;
	}


	public boolean isHighPriority() 
	{
		return highpriority;
	}


	public void setFilename(String filename)
	{
		this.filename = filename;
	}
	
	public String getFilename()
	{
		return filename;
	}
    
	public String getRealFilename()
	{
 
		String RealFile= StringUtil.environmentSubstitute(getFilename());
		return RealFile.replace('\\','/');
	}
	public void setSeparator(String separator)
	{
		this.separator = separator;
	}

	public void setEnclosed(String enclosed)
	{
		this.enclosed = enclosed;
	}
	public void setLineterminated(String lineterminated)
	{
		this.lineterminated = lineterminated;
	}

	public String getLineterminated()
	{
		return lineterminated;
	}
	public String getRealLineterminated()
	{
		return StringUtil.environmentSubstitute(getLineterminated());
	}

	public String getSeparator()
	{
		return separator;
	}
	public String getEnclosed()
	{
		return enclosed;
	}
    
	public String getRealSeparator()
	{
		return StringUtil.environmentSubstitute(getSeparator());
	}

	public String getRealEnclosed()
	{
		return StringUtil.environmentSubstitute(getEnclosed());
	}
	public void setLimitlines(String limitlines)
	{
		this.limitlines = limitlines;
	}
	public String getLimitlines()
	{
		return limitlines;
	}
    
	public String getRealLimitlines()
	{
		return StringUtil.environmentSubstitute(getLimitlines());
	}



	public void setListColumn(String listcolumn)
	{
		this.listcolumn = listcolumn;
	}
	public String getListColumn()
	{
		return listcolumn;
	}
    
	public String getRealListColumn()
	{
		return StringUtil.environmentSubstitute(getListColumn());
	}


	
}