/**
 * @UNCC Fodor Lab
 * @author Michael Sioda
 * @email msioda@uncc.edu
 * @date Feb 9, 2017
 * @disclaimer 	This code is free software; you can redistribute it and/or
 * 				modify it under the terms of the GNU General Public License
 * 				as published by the Free Software Foundation; either version 2
 * 				of the License, or (at your option) any later version,
 * 				provided that any use properly credits the author.
 * 				This program is distributed in the hope that it will be useful,
 * 				but WITHOUT ANY WARRANTY; without even the implied warranty of
 * 				MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * 				GNU General Public License for more details at http://www.gnu.org *
 */
package biolockj.module;

import java.io.File;
import java.util.List;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * Classes coded to the BioModule interface can be added to the to the BioLockJ
 * pipeline by including it in the Config file with the #BioLockJ tag.  The
 * BioLockJ.jar main-class [ biolockj.BioLockJ.java ] executes the BioModules
 * one at a time in the order provided in Config file. New BioModules can be
 * added into your pipeline by coding to this interface.  A suite of useful
 * methods to handle BioModules are available in biolockj.util.ModuleUtil.
 *
 */
public interface BioModule
{

	/**
	 * Before any BioModules are executed in the main program loop, this method
	 * will run 1st for all BioModules to validate any dependencies.  It is
	 * preferable to discover any missing or invalid properties prior to starting
	 * the main pipeline.
	 *
	 * @throws Exception thrown if missing or invalid dependencies are found
	 */
	public void checkDependencies() throws Exception;

	/**
	 * This method contains the code the BioModule executes to complete its task.
	 *
	 * @throws Exception thrown if the module is unable to complete is task
	 */
	public void executeProjectFile() throws Exception;

	/**
	 * Each BioModule takes the previous BioModule output as input:
	 * BioModule[ n ].getInputDir() = BioModule[ n - 1 ].getOutputDir()
	 *
	 * Special cases:
	 *
	 * The 1st BioModule to run will return the 1st path in Config.INPUT_DIRS
	 *
	 * BioModule biolockj.module.Metadata returns null and obtains its input
	 * from Config.INPUT_METADATA
	 *
	 * If biolockj.module.Metadata is the 1st BioModule (as is often the case),
	 * the 2nd BioModule will return the 1st path in Config.INPUT_DIRS
	 *
	 * @return File - A directory containing input files (or null)
	 */
	public File getInputDir();

	/**
	 * Each BioModule takes the previous BioModule output as input:
	 *
	 * BioModule[ n ].getInputFiles() = BioModule[ n - 1 ].getOutputDir().listFiles()
	 *
	 * Special cases:
	 *
	 * The 1st BioModule to run will return the files contained in the
	 * folders listed in Config.INPUT_DIRS.
	 *
	 * biolockj.module.Metadata returns only one file, Config.INPUT_METADATA
	 *
	 * If biolockj.module.Metadata is the 1st BioModule (as is often the case),
	 * the 2nd BioModule will return the files contained in the folders
	 * listed in Config.INPUT_DIRS.
	 *
	 * @return List<File> - Input files for the BioModule
	 *
	 * @Exception - thrown if unable to obtain the list of input files
	 */
	public List<File> getInputFiles() throws Exception;

	/**
	 * Many BioModules generate bash or R scripts for the Operating System to
	 * run as a biolockj.Job.  The Job creates a java.lang.Process using the
	 * parameters supplied by this method and executed in the java.lang.Runtime
	 * environment.  Parameters typically contain the full script path and
	 * script parameters, if needed.
	 *
	 * @return String[] - java.lang.Runtime.exec parameters
	 *
	 * @Exception - thrown if unable to build the job parameters
	 */
	public String[] getJobParams() throws Exception;

	/**
	 * Each BioModule configured generates sub-directory under
	 * Config.PROJECT_DIR.  All BioModule output must be contained within this
	 * directory and its sub-directories
	 *
	 * @return File - The BioModule root directory
	 */
	public File getModuleDir();

	/**
	 * All BioModule output that is to be made available for the next
	 * BioModule must be contained within this directory.
	 *
	 * @return File - A directory containing the primary BioModule output
	 */
	public File getOutputDir();

	/**
	 * BioModules that run scripts to complete their task, often by calling
	 * other command line bioinformatics tools such as bowtie2 or QIIME, output
	 * generated scripts to this directory.  The main-class biolockJ.BioLockJ
	 * looks for the main script, which must begin with MAIN_SCRIPT_PREFIX, in
	 * this directory and there must only be one main script.  The main script
	 * executes, or submits to the job queue, each of the other scripts found
	 * in this directory. The # subscripts generated by the BioModule typically
	 * depends upon Config.SCRIPT_BATCH_SIZE
	 *
	 * @return File - A directory containing all BioModule scripts
	 */
	public File getScriptDir();

	/**
	 * A succinct summary is very helpful since it is included in the email
	 * sent upon pipeline completion to the user (if biolockj.module.Email) is
	 * configured to run.  Including any specific data from the project is
	 * strongly discouraged to avoid the unintentional publication of
	 * confidential information.  However, meta-data such as number/size of
	 * input/output files can be helpful during debug, and also provides a
	 * convenient location to report summary statistics (average #reads,
	 * average processing time/file, min/max file size, etc).
	 *
	 * @return String - A summary of BioModule execution for reporting purposes
	 */
	public String getSummary();

	/**
	 * BioModules may generate intermediate files or data that can be stored as
	 * intermediate files but are not used by the next BioModule.  The files
	 * may contain supplementary information or data that may be helpful while
	 * debugging or recovering a failed pipeline.  The BioModule can create any
	 * sub-directory needed to store these files, but the temp folder is often
	 * used.  If ( Config property BioLockJ.PROJECT_DELETE_TEMP_FILES = Y ) &&
	 * the pipeline executes successfully, all BioModule temp directories
	 * are deleted.
	 *
	 * @return File - A directory of files typically not useful long term
	 */
	public File getTempDir();

	/**
	 * BioModules that run scripts can set a timeout value.
	 *
	 * @return int - # minutes before script is cancelled due to timeout
	 */
	public Integer getTimeout();

	/**
	 *  A BioModule may only need a subset of the files contained in the input
	 *  directory.  The method parameters are used to filter inputDir files and
	 *  inputDir sub-directories respectively. The main-class biolockj.BioLockJ
	 *  passes TrueFileFilter.INSTANCE & null for these parameters which
	 *  selects all files and ignores the sub-directories.
	 *
	 */
	public void initInputFiles( final IOFileFilter ff, final IOFileFilter recursive ) throws Exception;

	/**
	 * The main-class biolockj.BioLockJ calls this method to set the input of
	 * each BioModule to the output directory of the previous BioModule.  The
	 * BioModule will use the files contain in the parameter directory as input.
	 */
	public void setInputDir( final File dir );

	/**
	 * The main-class biolockj.BioLockJ calls this method to set the name of
	 * each BioModule root directory.  Module root directories are created
	 * directly under Config.PROJECT_DIR and are named starting with an integer
	 * corresponding to the order the BioModules are to be executed and are
	 * named after the Java class name of the BioModule.
	 *
	 */
	public void setModuleDir( final String name );

	public static final String BLJ_COMPLETE = "biolockjComplete";
	public static final String BLJ_STARTED = "biolockjStarted";
	public static final String FAILURE_DIR = "failure";
	public static final String MAIN_SCRIPT_PREFIX = "MAIN_";
	public static final String OUTPUT_DIR = "output";
	public static final String SCRIPT_DIR = "script";
	public static final String TEMP_DIR = "temp";

}
