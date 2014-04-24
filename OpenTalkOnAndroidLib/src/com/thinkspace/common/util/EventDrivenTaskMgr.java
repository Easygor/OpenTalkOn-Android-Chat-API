package com.thinkspace.common.util;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventDrivenTaskMgr extends Thread {
	Queue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
	
	@Override
	public void run(){
		try{
			while(true){
				Thread.sleep(1);
				synchronized (workQueue) {
					if(workQueue.isEmpty()){
						workQueue.wait();
					}
				}
				Runnable task = workQueue.poll();
				if(task != null){
					task.run();
				}
			}
		}catch(InterruptedException ex){
			ex.printStackTrace();
		}
	}
	
	public void addTask(Runnable run){
		synchronized (workQueue) {
			workQueue.add(run);
			if(this.getState() == State.WAITING){
				workQueue.notify();
			}
		}
	}
	
	public void stopTask(){
		this.interrupt();
	}
}
