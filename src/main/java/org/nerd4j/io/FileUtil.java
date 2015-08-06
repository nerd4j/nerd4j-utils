/*
 * #%L
 * Nerd4j Utils
 * %%
 * Copyright (C) 2011 - 2014 Nerd4j
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package org.nerd4j.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for {@link File} handling.
 * 
 * @author Nerd4j Team
 */
public final class FileUtil
{

	/** Logging system. */
	private static final Logger log = LoggerFactory.getLogger( FileUtil.class );
	
	/** Private constructor, no new instances */
	private FileUtil() { super(); }
	
	/**
	 * Creates the file and all the containing directories in the given path.
	 * <p>
	 *  This method assumes the leaf in the path to be a file.
	 *  If you want to create a directory use {@link FileUtil#createWholePath(String,boolean)}.
	 * </p>
	 * 
	 * @param path the file path to create.
	 * @return the new created {@link File} or {@code null} if the creation fails.
	 */
	public static File createWholePath( String path )
	{
		
		return createWholePath( new File(path), false );
		
	}

	/**
	 * Creates the file and all the containing directories in the given path.
	 * <p>
	 *  If the {@code isDirectory} flag is {@code true} even the leaf will be
	 *  created as a directory, otherwise will be a file with the given name.
	 * </p> 
	 * 
	 * @param path        the file path to create.
	 * @param isDirectory tells if the leaf should be a directory.
	 * @return the new created {@link File} or {@code null} if the creation fails.
	 */
	public static File createWholePath( String path, boolean isDirectory )
	{
		
		return createWholePath( new File(path), isDirectory );
		
	}
	
	/**
	 * Creates the file and all the containing directories in the given path.
	 * <p>
	 *  This method assumes the leaf in the path to be a file.
	 *  If you want to create a directory use {@link FileUtil#createWholePath(String,boolean)}.
	 * </p>
	 * 
	 * @param path the file path to create.
	 * @return the new created {@link File} or {@code null} if the creation fails.
	 */
	public static File createWholePath( File path )
	{
		
		return createWholePath( path, false );
		
	}
	
	/**
	 * Creates the file and all the containing directories in the given path.
	 * <p>
	 *  If the {@code isDirectory} flag is {@code true} even the leaf will be
	 *  created as a directory, otherwise will be a file with the given name.
	 * </p> 
	 * 
	 * @param path        the file path to create.
	 * @param isDirectory tells if the leaf should be a directory.
	 * @return the new created {@link File} or {@code null} if the creation fails.
	 */
	public static File createWholePath( File path, boolean isDirectory )
	{
		
		if( path == null || path.getPath().isEmpty() )
			throw new NullPointerException();
		
		try{
			
			
			/*
			 * We create the absolute path related to the given path (that can
			 * be a relative path)
			 */
			final File absolutePath = path.getAbsoluteFile();
			
			/* 
			 * If the file or the directory identified by the
			 * given path already exists no other work needs
			 * to be done.
			 */
			if( path.exists() )
			{
				
				/* Check right type */
				if ( (isDirectory && !path.isDirectory()) ||  (!isDirectory && !path.isFile()) )
					throw new IOException( "Unable to create the path " + absolutePath + 
							               ", it already exists but has the wrong leaf type." );
				
				return path;
				
			}
			
			if( log.isInfoEnabled() )
			{
				final String type = isDirectory ? "directory" : "file";
				log.info( "Going to create the {} {} and all its parent directories.", type, absolutePath );
			}
			
			if( isDirectory )
			{
				
				log.debug( "Going to create all the directories in the path." );
				
				/*
				 * We already checked that the directory does not exist
				 * so if the method mkdirs() returns false it means that
				 * it's not possible to create one of the directories
				 * in the path because such path is impossible or because
				 * writing privileges are insufficient.
				 */
				if( ! absolutePath.mkdirs() )
					throw new IOException( "Unable to create one directory in the path " + absolutePath + 
							               ", check it for writing rights." );
				
			}
			else
			{
			
				final File parentDir = absolutePath.getParentFile();
				log.debug( "Retrieving parent directory {}", parentDir );
			
				/*
				 * If the parent directory does not exists and we
				 * are not able to create one of the directories
				 * in the path an IOException will be thrown.
				 */
				log.debug( "Going to create all the parent directories." );
				if( ! parentDir.exists() && ! parentDir.mkdirs() )
					throw new IOException( "Unable to create one directory in the path " + parentDir.getPath() +
							               ", check it for writing rights." );

				/*
				 * We already checked that the file does not exist
				 * so if the method createNewFile() returns false
				 * it means that we have insufficient writing
				 * privileges in the parent directory.
				 */
				log.debug( "Going to create the leaf file." );
				if( ! absolutePath.createNewFile() )
					throw new IOException( "Unable to create the file " + absolutePath.getName() +
							               ", check the directory " + parentDir.getPath() +
							               " for writing rights." );
				
			}
					
			return absolutePath;
			
		}catch( IOException ex )
		{
			
			log.warn( "Error occured during path creation: ", ex );
			return null;
			
		}
		
	}
	
	
	/**
	 * Performs a copy from the given source file to the provided
	 * destination file.
	 * <p>
	 *  Both files must exist.
	 * </p>
	 * 
	 * @param src data source.
	 * @param dst copy destination.
	 * 
	 * @return {@code true} if the copy succeed.
	 */
	public static boolean copy( File src, File dst )
	{
		
		try
		{
			
			/* We check the existence of the source file. */
			if ( src == null || ! src.exists() )
			{
				final String path = src == null ? null : src.getPath();
				throw new IOException( "Source file " + path + " doesn't exists." );
			}
			
			/* We check the existence of the target file. */
			if ( dst == null || ! dst.exists() )
			{
				final String path = dst == null ? null : dst.getPath();
				throw new IOException( "Destination file " +  path + " doesn't exists." );
			}
	
			FileChannel source      = null;
			FileChannel destination = null;
			
			try
			{
				
				/* File copy using the java NIO facility. */
				source      = new FileInputStream(src).getChannel();
				destination = new FileOutputStream(dst).getChannel();
				
				destination.transferFrom(source, 0, source.size());
				
				return true;
				
			} finally
			{
				
				/* 
				 * We do not consider a failure if the source file cannot be closed
				 * as long as the whole data was successfully copied.
				 */
				if ( source != null )
					CloseableUtil.closeAndSoak( source, "Cannot properly close source file {} channel.", src.getName() );
				
				if ( destination != null )
					destination.close();
				
			}
			
		} catch( IOException ex )
		{
			
			log.warn( "Error occured during file copy: ", ex );
			return false;
			
		}
		
	}
	
	/**
	 * Deletes the given file or directory.
	 * <p>
	 *  If the given {@link File} represents a directory all
	 *  the content of the directory will be deleted.
	 * </p>
	 * 
	 * @param file the {@link File} to be deleted.
	 * @return {@code true} if the remove was performed with success.
	 */
	public static boolean delete( File file )
	{
		
		
		/* If the file does not exists nothing will be done. */
		if ( file == null || ! file.exists() )
		{
			final String path = file == null ? null : file.getPath();
			log.debug( "Nothing to delete; file {} doesn't exist.", path );
			return true;
		}
		
		/*
		 * Abstract path of the file, this path is always values
		 * also if the file actually does not exist.
		 */
		final String path = file.getPath();
		
		log.debug( "Deleting {}.", path );
		
		try
		{
			
			/* If it is a directory first we need to remove its content. */
			if ( file.isDirectory() )
			{
				
				log.debug( "Deleting directory {} children.", path );
				
				for( File child : file.listFiles() )
				{
					
					/* 
					 * The delete fails if we are not able to remove
					 * one of the elements in the directory.
					 */
					if ( !delete( child ) )
					{
						log.warn( "Cannot delete {} child {}.", path, child.getPath() );
						return false;
					}
					
				}
				
			}
			
			/* Fails if we are not able to remove the current file. */
			if ( !file.delete() )
			{
				log.warn( "Cannot delete {}.", path );
				return false;
			}
		
		} catch( SecurityException e )
		{
			
			log.warn( "Cannot delete {}.", path, e );
			return false;
			
		}
		
		return true;
		
	}
	
	/**
	 * Moves the data from the given source file to the provided
	 * destination file.
	 * <p>
	 *  The source file must exist.
	 * </p>
	 * 
	 * @param src data source.
	 * @param dst data destination.
	 * 
	 * @return {@code true} if the move succeed.
	 */
	public static boolean move( File src, File dst )
	{
		
		/* We check the existence of the source file. */
		if ( src == null || ! src.exists() )
		{
			final String path = src == null ? null : src.getPath();
			log.debug( "Source file {} doesn't exists.", path );
			return false;
		}
		
		/*
		 * First we check if the destination file already exists,
		 * in this case we need to delete it.
		 */
		if ( !delete( dst ) )
			return false;

		/* Than we try to rename the source file. */
		if ( src.renameTo( dst ) )
			return true;
		
		/* If the rename fails we need to perform a file copy. */
		
		try
		{
			
			dst.createNewFile();
			
		} catch ( IOException e )
		{
			
			log.warn( "Cannot touch {}", dst.getName(), e );
			
			return false;
			
		}
		
		/* The file move succeed if both the copy and the delete succeed. */
		return copy( src, dst ) && delete( src );
		
	}
	
}