/*
 * #%L
 * Nerd4j Utils
 * %%
 * Copyright (C) 2011 - 2015 Nerd4j
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
package org.nerd4j.thread;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.nerd4j.util.Require;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The aim of this class is to handle the asynchronous execution 
 * of a given list of tasks all working on the same resource pool. 
 * 
 * <p>
 * The basic concept of this class is that the given tasks can be
 * many and possibly time consuming while the accessed resources
 * are limited.
 *  
 * <p>
 * A classical use case is scheduling a list of task where each
 * one needs to call a remote service through a connection pool.
 *  
 * <p>
 * This class is initialized with an {@link ExecutorService} and
 * a positive integer {@code n} representing the number of available
 * resources.  The  {@link ExecutorService} is used to run
 * asynchronously the given tasks ensuring that no more than {@code n}
 * task are executed concurrently.
 * 
 * <p>
 * When a list of tasks is submitted for execution no other list can
 * be submitted until each task in the first list has been executed.
 * The tasks are started in the order in which they are submitted
 * unless the order is changed by the invocation of the 
 * {@link #getOrWaitForResult(Callable)} method as described later.
 * 
 * <p>
 * This class provides a method to get or wait for the result of
 * a given task. The provided {@link Callable} are used as keys in
 * an internal {@link Map} so they need to implement properly the
 * methods {@link #hashCode()} and {@link #equals(Object)}.
 * The result of the executed tasks are kept in the internal {@link Map}
 * until another list is submitted or until the internal status
 * is cleaned using the proper {@link #clear()} method.
 *  
 * <p>
 * The method {@link #getOrWaitForResult(Callable)} returns the same
 * object returned by the {@link Callable#call()} method. Actually
 * {@link Callable#call()} is invoked at some point during the
 * asynchronous execution of the task.
 * Calling the {@link #getOrWaitForResult(Callable)} method will lead to
 * three different execution flows:
 * <ol>
 *  <li>Returns immediately if the related task execution has been completed.</li>
 *  <li>Waits for the task to complete if it is currently executing.</li>
 *  <li>Schedules the task to be the next to be executed and waits until is completed.</li>
 * </ol>
 * 
 * @author Nerd4j Team
 */
public class BoundedResourcesAsyncTaskExecutor
{
	
	/** Internal logging system. */
	private static final Logger log = LoggerFactory.getLogger( BoundedResourcesAsyncTaskExecutor.class );
	
	
	/** Executor service to use for asynchronous executions. */
	private final ExecutorService executorService;
	
	/** 
	 * Semaphore used to limit the number of concurrent threads
	 * to the number of available resources.
	 */
	private final Semaphore resourceSemaphore;
	
	/**
	 * Semaphore used to serialize the execution of the tasks.
	 * This semaphore is used to keep control over the order
	 * in which the tasks are executed, a new task is submitted
	 * to the {@link ExecutorService} only after the last
	 * submitted task is started. 
	 */
	private final Semaphore startNextSemaphore;
	
	/** 
	 * Internal task that runs asynchronously and handles
	 * che {@link #taskQueue}. */
	private TaskQueueHandler taskQueueHandler;
	
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param executorService service for asynchronous task execution.
	 * @param resourceLimit   number of available resources.
	 */
	public BoundedResourcesAsyncTaskExecutor( ExecutorService executorService, int resourceLimit )
	{
		
		super();
		
		
		Require.toHold( resourceLimit > 0, "Resource limit must be > 0" );
		
		this.executorService  = Require.nonNull( executorService, "Executor service must be not null" );;
		this.taskQueueHandler = null;
		
		this.startNextSemaphore = new Semaphore( 1 );
		this.resourceSemaphore = new Semaphore( resourceLimit );
				
	}
	
	
	
	/**
	 * Executes asynchronously the given list of tasks, performing
	 * a number of concurrent executions up to the limit of available
	 * resources. The given tasks are executed in the same order
	 * in which they are provided.
	 * <p>
	 * <b>Note</b> This method expects all the provided tasks to be
	 * all different. Internally a {@link Map} is used to keep the
	 * results of the executed tasks to if the same task occurs
	 * multiple times an {@link IllegalArgumentException} will be
	 * thrown. 
	 * 
	 * @param tasks list of tasks to be executed.
	 * @throws IllegalStateException if a previous list of tasks is still executing.
	 * @throws IllegalArgumentException if the same task occurs multiple times in the list.
	 */
	public synchronized void execute( Collection<? extends Callable<?>> tasks )
	{
		
		/*
		 * This local assignment prevent the taskQueueHandler
		 * to be set to null by another thread while invoking
		 * the isRunning() method. 
		 */
		if( isRunning() ) throw new IllegalStateException( "Current execution is still running." );
		
		final TaskQueueHandler newHandler = new TaskQueueHandler( tasks );
		executorService.execute( newHandler );
		
		this.taskQueueHandler = newHandler;
		
	}
	
	
	/**
	 * Returns the result of the task execution if completed.
	 * <p>
	 * Invoking this method will lead to three different execution flows:
	 * <ol>
	 *  <li>Returns immediately if the related task execution has been completed.</li>
	 *  <li>Waits for the task to complete if it is currently executing.</li>
	 *  <li>Schedules the task to be the next to be executed and waits until is completed.</li>
	 * </ol>
	 * 
	 * @param task the task to get the result for.
	 * @param <R> type of the result. 
	 * @return the execution result if completed.
	 * @throws InterruptedException  if the thread is interrupted.
	 * @throws ExecutionException    if the task execution fails.
	 * @throws CancellationException if the task execution is cancelled.
	 */
	public <R> R getOrWaitForResult( Callable<R> task )
	throws InterruptedException, ExecutionException, CancellationException
	{
		
		if( task == null )
			throw new NullPointerException( "The required task must ne not null." );
			
		final Future<R> execution = getCurrentQueueHandler().executeAsNext( task );
				
		/*
		 * If the task related execution is null it means that
		 * the required task is not in the list provided to the
		 * method execute and therefore not scheduled for execution.
		 */
		if( execution == null )
			throw new NullPointerException( "The given task " + task + " is not scheduled for execution." );
			
		/*
		 * Otherwise we wait until execution end and return
		 * the execution result.
		 */
		return execution.get();
		
	}
	
	
	/**
	 * Tells if all the provided tasks have been executed.
	 * 
	 * @return {@code true} if task are currently executing or completed.
	 */
	public boolean isRunning()
	{
		
		/*
		 * This local assignment prevent the taskQueueHandler
		 * to be set to null by another thread while invoking
		 * the isRunning() method. 
		 */
		final TaskQueueHandler currentHandler = taskQueueHandler;
		return currentHandler != null && currentHandler.isRunning();
		
	}
	
	
	/**
	 * Tells if all the provided tasks have been executed.
	 * 
	 * @return {@code true} if task are currently executing or completed.
	 */
	public boolean isCompleted()
	{
		
		return getCurrentQueueHandler().isCompleted();
		
	}
	
	
	/**
	 * Forces the current execution to stop.
	 * <p>
	 * Prevents waiting tasks to be executed
	 * but doesn't stop the ones already started.
	 */
	public void stopCurrentExecution()
	{
		
		getCurrentQueueHandler().stop();
		
	}

	
	/**
	 * Even if stopped or completed the system keeps
	 * the results of all executed tasks.
	 * This method tells to clean the internal state.
	 */
	public void clear()
	{
		
		this.taskQueueHandler = null;
		
	}

	
	/* ***************** */
	/*  PRIVATE METHODS  */
	/* ***************** */

	
	/**
	 * Returns the current {@link TaskQueueHandler} if available
	 * otherwise throws an {@link IllegalStateException}.
	 * 
	 * @return the current {@link TaskQueueHandler} if available.
	 * @throws IllegalStateException if there are no current queue handlers.
	 */
	private TaskQueueHandler getCurrentQueueHandler()
	{
		
		final TaskQueueHandler currentQueueHandler = taskQueueHandler;
		if( currentQueueHandler == null )
			throw new IllegalStateException( "There are no running executions or execution status was cleared." );
		
		return currentQueueHandler;
		
	}
	
	
	/* *************** */
	/*  INNER CLASSES  */
	/* *************** */
	
	
	/**
	 * Internal forker used to schedule task executions.
	 * 
	 * @author Nerd4j Team
	 */
	private class TaskQueueHandler implements Runnable
	{
		
		/** Indica se il task è terminato o è stato stoppato. */
		private final AtomicBoolean stopped;
		
		/** Map that associates each task to the related execution. */
		private final Map<Callable<?>,Future<?>> executionMap;
		
		/** Queue of the tasks to be executed. */
		private final Queue<Callable<?>> taskQueue;
		
		
		/**
		 * Default constructor.
		 * 
		 * @param taskList list of tasks to be executed.
		 */
		public TaskQueueHandler( Collection<? extends Callable<?>> taskList )
		{
			
			super();
			
			Require.nonEmpty( taskList, "Task list must be not empty" );
			
			this.stopped = new AtomicBoolean();
			this.taskQueue = new LinkedList<Callable<?>>();
			this.executionMap = new HashMap<Callable<?>, Future<?>>( taskList.size() );
			
			for( Callable<?> task : taskList )
			{
				
				Require.nonNull( task, "Task must be not null" );
				
				if( executionMap.containsKey(task) )
				{
					log.error( "Task {} submitted multiple times in task list.", task );
					throw new IllegalArgumentException( "The same task cannot occur multiple times in the task list." );
				}
					
				executionMap.put( task, null );
				taskQueue.add( task );
				
			}
			
		}
		
		
		/* ******************* */
		/*  INTERFACE METHODS  */
		/* ******************* */

		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run()
		{
			
			
			try{
				
				boolean hasNext = false;
				do{
	
						/*
						 * Executes the next task in che queue.
						 * If the method executeNext() returns false
						 * there are no more task to execute.
						 */
						hasNext = executeNext();
								
				}while( ! stopped.get() && hasNext );
				
			}catch( InterruptedException  ex )
			{
					
				log.error( "Download queue has been interrupted.", ex );
					
			}finally
			{
				
				if( taskQueue.isEmpty() )
					log.info( "Task queue execution completed." );
				else
					log.info( "Premature stop of task queue execution." );
				
			}
			
		}
		
		
		/**
		 * Tells the {@link TaskQueueHandler} to force the given task
		 * to be executed as next.
		 * <ul>
		 *  <li>If the task has already been executed returns the related {@link Future}.</li>   
		 *  <li>If the task is currently running returns the related {@link Future}.</li>
		 *  <li>If the task was not executed yet starts the execution and returns the related {@link Future}.</li>
		 * </ul>
		 *  
		 * @param task the task to be executed as next.
		 * @return the {@link Future} related to the task execution.
		 * @throws InterruptedException if the task is interrupted
		 * during execution.
		 */
		public synchronized <R> Future<R> executeAsNext( Callable<R> task )
		throws InterruptedException
		{
			
			@SuppressWarnings("unchecked")
			final Future<R> execution = (Future<R>) executionMap.get( task );
			if( execution != null ) return execution;
			
			if( stopped.get() )
				throw new IllegalStateException( "Executions has been stopped, can't start any new task." );
			
			return taskQueue.remove( task ) ? startExecution( task ) : null;
			
		}
		
		
		/**
		 * Tells if this queue handler is currently running.
		 * 
		 * @return {@code true} if currently running.
		 */
		public boolean isRunning()
		{
			
			return ! stopped.get() && ! taskQueue.isEmpty();
			
		}
		
		
		/**
		 * Tells if all the tasks in the queue have been handled.
		 * 
		 * @return {@code true} if the task queue is empty.
		 */
		public boolean isCompleted()
		{
			
			return taskQueue.isEmpty();
			
		}
		
		
		/**
		 * Prevent the TaskQueueHandler from executing other
		 * tasks and changes his status into 'stopped'.
		 * However this method doesn't affect the currently
		 * executed tasks.
		 */
		public void stop()
		{
			
			stopped.set( true );
			
		}
		
		
		/* ***************** */
		/*  PRIVATE METHODS  */
		/* ***************** */
		
		
		/**
		 * Puts the next task in teh queue into execution.
		 * 
		 * @return {@code true} if there was a new task to execute.
		 * @throws InterruptedException if the task is interrupted
		 * during execution.
		 */
		private synchronized boolean executeNext()
		throws InterruptedException
		{
				
			final Callable<?> task = taskQueue.poll();
			if( task == null ) return false;
					
			startExecution( task );
			return true;
			
		}
		
		
		/**
		 * Questo metodo non è pensato per essere usato direttamente
		 * ma solo all'interno dei sui metodi sincronizzati
		 * {@link #downloadNext()} e {@link #downloadAsNext(Couple)}
		 * 
		 * @param download il download da eseguire.
		 * @return il future relativo all'esecuzione del download.
		 */
		private <R> Future<R> startExecution( Callable<R> task )
		throws InterruptedException
		{
			
			/* 
			 * We acquire the right to start a new execution.
			 * No other threads can start task executions until
			 * this semaphore will be released by the TaskWrapper
			 * when the related execution starts.
			 */
			log.debug( "Starting next task {}", task );
			startNextSemaphore.acquire();
			
			/* The task wrapper used to synchronize execution. */ 
			final TaskWrapper<R> wrapper = new TaskWrapper<R>( task, stopped );
			
			/* We submit the task to the executor to be assigned to a thread. */
			final Future<R> execution = executorService.submit( wrapper );
			
			/*
			 * We update the executionMap with the Future representing
			 * the actual execution of the related task.
			 */
			executionMap.put( task, execution );
			
			/* Finally we return the Future for further use. */
			return execution;
			
		}
		
	}
	
	
	/**
	 * Internal wrapper used to synchronize task execution.
	 * 
	 * @author Nerd4j Team
	 */
	private class TaskWrapper<R> implements Callable<R>
	{
		
		/** Actual task to be executed. */
		private final Callable<R> task;
		
		/** Tells if, in the meanwhile the execution has been stopped. */ 
		private final AtomicBoolean stopped;
				
		
		/**
		 * Constructor with parameters.
		 * 
		 * @param task    the actual task to be executed.
		 * @param stopped tells if the execution has benn stopped.
		 */
		public TaskWrapper( Callable<R> task, AtomicBoolean stopped )
		{
			
			super();
			
			Require.nonNull( stopped );
			Require.nonNull( task, "Task must be not null" );
			
			this.task = task;
			this.stopped = stopped;
			
		}
		

		/**
		 * {@inheritDoc}
		 */
		@Override
		public R call() throws Exception
		{
			
			try{
			
				log.debug( "Acquiring resource for task {}...", task );
				resourceSemaphore.acquire();
				
			}catch( InterruptedException ex )
			{
				
				log.warn( "Catched an InterruptedException while acquiring resource.", ex );
				throw ex;
			
			}finally
			{
			
				/* 
				 * Both if it is executed or interrupted the task can be
				 * considered to be started, so we can release the block
				 * that prevent other tasks to be started.
				 */
				startNextSemaphore.release();
				
			}
			
			try{
				
				/*
				 * If during acquisition of the resource the task
				 * execution has been stopped we do not proceed.
				 */
				if( stopped.get() ) return null;
				
				log.debug( "Executing provided task {}", task );
				
				final R result = task.call();
				
				log.debug( "Task {} completed.", task );
				
				return result;
				
			
			}finally
			{
				
				resourceSemaphore.release();			
				log.debug( "Resource released by task {}.", task );
				
			}
			
		}
		
	}
	
}
