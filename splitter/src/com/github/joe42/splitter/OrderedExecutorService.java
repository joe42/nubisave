package com.github.joe42.splitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
/**
 * @author joe
Ich habe eine PriorityBlockingQueue Warteschlange und eine Collections.synchronizedList() auf die von mehreren Threads aus zugegriffen wird. Kann ich nun jeweils einen synchronized Block darauf setzen und davon ausgehen, dass innerhalb meines Blocks kein anderer Thread auf die Collection zugreift oder werden die Klasse auf andere Art (interne Locks) synchronisiert?
Es ist wichtig, dass ich mehrere Methoden ausführen kann, ohne dass die Warteschlange zwischendurch verändert wird. 
Ich versuche mal grob das Problem zu erläutern. Ich habe eine Klasse RefinedExecutor geschrieben und will ihr Tasks hinzufügen. RefinedExecutor führt Tasks mithilfe einer Java Executor Instanz aus.
Die Tasks haben einen String und einen Dateinamen. Wenn sie ausgeführt werden schreiben sie die entsprechende Datei mit dem String als Inhalt auf Platte. 
Die PriorityBlockingQueue priorityQueue wird von einem Executor als Warteschlange verwendet. Dann gibt es eine "SynchronizedList" aktiveElements, die die laufenden Tasks des Executors anzeigt. Und eine weitere SynchronizedList standbyElements, die unabhängig von der Executor Instanz ist.
Im Prinzip kommt ein Element in priorityQueue und aktiveElements nur einmal vor, damit das gleiche Element nie zeitgleich ausgeführt wird. Man kann davon ausgehen, dass der parallele Schreibzugriff auf eine Datei zu Inkonsistenten Ergebnissen führt. Wenn möglich sollen beim Hinzufügen neuer Tasks andere Tasks entfernt werden, die auf die gleiche Datei schreiben, da es darauf ankommt, was zuletzt in die Datei geschrieben wurde und nicht darauf, dass alle Inhalte in die Datei geschrieben wurden.
Wenn ich ein Element "hinzufüge" wird es zuerst aus standbyElements und priorityQueue entfernt, um sicherzustellen, dass es nicht doppelt vorkommt (Taks mit gleichem Dateinamen). Aus aktiveElements kann ich es nicht entfernen, da die Liste nur zeigt, ob der Task läuft und ansonsten vom Executor gekapselt wird.
Wenn das Element in aktiveElements ist, kommt es zu den standbyElements, damit es zumindest nicht zeitgleich vom Executor ausgeführt werden kann. Kommt das Element noch nicht vor, wird es zur priorityQueue hinzugefügt.
Wenn ein Task fertig ist, guckt er kurz vorher in die standbyElements und verschiebt falls vorhanden den gleichartigen Task in die priorityQueue.

Nun habe ich ein Problem beim hinzufügen des Tasks:
1. Check ob Task in aktiveElements ist -> ja (ich kann ihn aber nicht entfernen, also kommt er in die standbyList)
2. der Task ist fertig und guckt in die waitingQueue um gleichartige Tasks zu starten (das passiert nebenläufig, sollte aber entweder vor Schritt 1 oder nach Schritt 3 passieren)
3. der Task wird zu waitingQueue hinzugefügt und verlässt sich darauf, dass er von einem aktiven Task gestartet wird, was aber nie mehr passiert

 */
//TODO: exponential back-up queue for failed tasks
public class OrderedExecutorService {
	private ThreadPoolExecutor executor;
	private int nrOfThreads;
	private Semaphore unusedQueueElements;
	private List<OrderedRunnable> runningTasksList, waitingTaskList;
	private Object waitingTaskListLock = new Object();
	//private static final Logger  log = Logger.getLogger("OrderedExecutorService");
	public OrderedExecutorService(int nrOfThreads, int MAX_NUMBER_OF_ELEMENTS){
		unusedQueueElements = new Semaphore(MAX_NUMBER_OF_ELEMENTS);
		this.nrOfThreads = nrOfThreads;
		executor = new ThreadPoolExecutor(nrOfThreads, nrOfThreads,10, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>());
		runningTasksList = (List<OrderedRunnable>)Collections.synchronizedList(new ArrayList<OrderedRunnable>());
		waitingTaskList = (List<OrderedRunnable>)Collections.synchronizedList(new ArrayList<OrderedRunnable>());
	}
	/**
	 * Wraps instance of Runnable, so that it can be ordered according to index.
	 * Note: The natural order is inconsistent with equals.
	 */
	private class OrderedRunnable implements Runnable, Comparable<OrderedRunnable>{
		private Runnable core;
		private Long index;
		public OrderedRunnable(Runnable core, Long index){
			this.core = core;
			this.index = index;
		}
		@Override
		public void run() {
			runningTasksList.add(this);
			try{
			core.run();
			} catch(Exception e){
				System.out.println("some error");
			}
			unusedQueueElements.release();
			synchronized (waitingTaskList) {//no task must be added by OrderedExecutorService based on a running equal task
				runningTasksList.remove(this);
				OrderedRunnable taskToRun = null;
				for(OrderedRunnable task: waitingTaskList){
					if(task.equals(this)) {
						executor.execute(taskToRun);
						taskToRun = task;
						break;
					}
				}
				waitingTaskList.remove(taskToRun); //So that it does not interfere with running split
			}
		}
		
		@Override
		public int compareTo(OrderedRunnable arg0) {
			return index.compareTo(arg0.index);
		}
		
		@Override
		public boolean equals(Object obj) {
			if( obj == null || !(obj instanceof OrderedRunnable)){
				return false;
			}
			return ((OrderedRunnable)obj).core.equals(core);
		}
	}
	/**
	 * Submit task with System.currentTimeMillis() as priority for FIFO semantics, if there is not yet .
	 * @param task
	 */
	public void submit(Runnable task){
		OrderedRunnable taskToAdd = new OrderedRunnable(task, System.currentTimeMillis());
		waitingTaskList.remove(taskToAdd);
		executor.getQueue().remove(taskToAdd);
		System.out.println("AQUIRE element from queue: "+unusedQueueElements.availablePermits());
		try {
			unusedQueueElements.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		synchronized (waitingTaskList) {//if running task ends before adding new task to waitinglist it will not be executed
			if(runningTasksList.contains(taskToAdd)) { 
				waitingTaskList.add(taskToAdd); //So that it does not interfere with running split and is moved to executor queue, as soon as the equivalent running task ends
				return;
			}
		}
		executor.execute(taskToAdd);
		System.out.println("executor queue size: "+executor.getQueue().size()+"\n");
	}
	/**
	 * @param task
	 * @param priority low value = high priority
	 */
	public void submit(Runnable task, long priority){ 
		OrderedRunnable taskToAdd = new OrderedRunnable(task, priority);
		waitingTaskList.remove(taskToAdd);
		executor.getQueue().remove(taskToAdd);
		System.out.println("AQUIRE element from queue: "+unusedQueueElements.availablePermits());
		try {
			unusedQueueElements.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(runningTasksList.contains(taskToAdd)) {
			waitingTaskList.add(taskToAdd); //So that it does not interfere with running split
			return;
		}
		executor.execute(taskToAdd);
		System.out.println("executor queue size: "+executor.getQueue().size()+"\n");
	}
}
