package com.harmony.umbrella.scheduling;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.harmony.umbrella.log.Log;
import com.harmony.umbrella.log.Logs;

/**
 * Scheduler基础抽象类
 * 
 * @author wuxii@foxmail.com
 */
public abstract class AbstractScheduler<T extends Scheduler.JobInfo> implements Scheduler {

	protected static final Log log = Logs.getLog(AbstractScheduler.class);

	protected Map<String, T> jobInfoMap = new HashMap<String, T>();

	/**
	 * job工厂<p> 工厂中的内容包含了<li>job的类<li>job的触发规则<li>实例化job
	 * 
	 * @return
	 */
	protected abstract JobFactory getJobFactory();

	/**
	 * 初始化方法，用于加载job
	 * 
	 * @throws SchedulerException
	 */
	protected abstract void init() throws SchedulerException;

	/**
	 * 根据jobName启动对应的任务 <p> 只有在job存在并且还未启动才调用
	 * 
	 * @param jobName
	 * @param jobInfo
	 */
	protected abstract void doStart(String jobName, T jobInfo);

	/**
	 * 根据jobName关闭对应的任务 <p> 只有在job存在并且还未停止才调用
	 * 
	 * @param jobName
	 * @param jobInfo
	 */
	protected abstract void doStop(String jobName, T jobInfo);

	/**
	 * 根据jobName挂起对应的任务 <p> 只有在job存在并且还未挂起才调用
	 * 
	 * @param jobName
	 * @param jobInfo
	 */
	protected abstract void doPause(String jobName, T jobInfo);

	/**
	 * 根据jobName恢复挂起的任务 <p> 只有在job存在并且是挂起才调用
	 * 
	 * @param jobName
	 * @param jobInfo
	 */
	protected void doResume(String jobName, T jobInfo) {
		// empty implement
	}

	/**
	 * 在销毁Scheduler时候调用
	 */
	protected void destory() {
		for (JobInfo jobInfo : jobInfoMap.values()) {
			try {
				jobInfo.dump();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		stopAll();
	}

	@Override
	public void start(String jobName) {
		if (hasJob(jobName) && !isStarted(jobName)) {
			T jobInfo = jobInfoMap.get(jobName);
			doStart(jobName, jobInfo);
			jobInfo.setJobStatus(Status.START);
			return;
		}
		log.info("can't start job {} not find", jobName);
	}

	@Override
	public void stop(String jobName) {
		if (hasJob(jobName) && !isStopped(jobName)) {
			T jobInfo = jobInfoMap.get(jobName);
			doStop(jobName, jobInfo);
			jobInfo.setJobStatus(Status.STOP);
			return;
		}
		log.info("can't stop job {} not find", jobName);
	}

	@Override
	public void pause(String jobName) {
		if (hasJob(jobName) && !isPaused(jobName)) {
			T jobInfo = jobInfoMap.get(jobName);
			doPause(jobName, jobInfo);
			jobInfo.setJobStatus(Status.PAUSE);
			return;
		}
		log.info("can't pause job {} not find", jobName);
	}

	@Override
	public void resume(String jobName) {
		if (hasJob(jobName) && isPaused(jobName)) {
			T jobInfo = jobInfoMap.get(jobName);
			doResume(jobName, jobInfo);
			jobInfo.setJobStatus(Status.START);
			return;
		}
		log.info("can't resume job {} not find", jobName);
	}

	@Override
	public void restart(String jobName) {
		stop(jobName);
		start(jobName);
	}

	@Override
	public void restartAll() {
		stopAll();
		startAll();
	}

	@Override
	public void startAll() {
		for (String jobName : getJobNames()) {
			start(jobName);
		}
	}

	@Override
	public void stopAll() {
		for (String jobName : getJobNames()) {
			stop(jobName);
		}
	}

	@Override
	public void resumeAll() {
		for (String jobName : getJobNames()) {
			resume(jobName);
		}
	}

	@Override
	public void pauseAll() {
		for (String jobName : getJobNames()) {
			pause(jobName);
		}
	}

	/**
	 * 给出所有在启动的时{@linkplain #init()}加载job名称
	 * 
	 * @return
	 */
	public String[] getJobNames() {
		Set<String> names = jobInfoMap.keySet();
		return names.toArray(new String[names.size()]);
	}

	public JobInfo getJobInfo(String jobName) {
		return jobInfoMap.get(jobName);
	}

	@Override
	public boolean isReady(String jobName) {
		return Status.READY.equals(getJobStatus(jobName));
	}

	@Override
	public boolean isStarted(String jobName) {
		return Status.START.equals(getJobStatus(jobName));
	}

	@Override
	public boolean isStopped(String jobName) {
		return Status.STOP.equals(getJobStatus(jobName));
	}

	@Override
	public boolean isPaused(String jobName) {
		return Status.PAUSE.equals(getJobStatus(jobName));
	}

	@Override
	public Status getJobStatus(String jobName) {
		JobInfo jobInfo = jobInfoMap.get(jobName);
		return jobInfo != null ? jobInfo.getJobStatus() : null;
	}

	@Override
	public boolean hasJob(String jobName) {
		return jobInfoMap.containsKey(jobName);
	}

}
