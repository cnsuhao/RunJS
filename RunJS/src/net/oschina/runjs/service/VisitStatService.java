package net.oschina.runjs.service;

import java.util.*;
import java.util.concurrent.*;

import net.oschina.common.db.DBManager;
import net.oschina.runjs.beans.VisitStat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 访问统计服务
 * 
 * @author Winter Lau
 * @date 2011-1-6 下午03:49:40
 */
public class VisitStatService extends TimerTask {

	private final static Log log = LogFactory.getLog(VisitStatService.class);
	private static boolean start = false;
	private static VisitStatService daemon;
	private static Timer click_timer;
	private final static long INTERVAL = 300 * 1000;

	/**
	 * 支持统计的对象类型
	 */
	private final static byte[] TYPES = new byte[] { VisitStat.TYPE_CODE,VisitStat.TYPE_SQUARE };

	// 内存队列
	private final static ConcurrentHashMap<Byte, ConcurrentHashMap<Long, Integer>> queues = new ConcurrentHashMap<Byte, ConcurrentHashMap<Long, Integer>>() {
		{
			for (byte type : TYPES)
				put(type, new ConcurrentHashMap<Long, Integer>());
		}
	};

	/**
	 * 记录访问统计
	 * 
	 * @param type
	 * @param obj_id
	 */
	public static void record(byte type, long obj_id) {
		ConcurrentHashMap<Long, Integer> queue = queues.get(type);
		if (queue != null) {
			Integer nCount = queue.get(obj_id);
			nCount = (nCount == null) ? 1 : nCount + 1;
			queue.put(obj_id, nCount.intValue());
			// System.out.printf("record (type=%d,id=%d,count=%d)\n",type,obj_id,nCount);
		}
	}

	/**
	 * 启动统计数据写入定时器
	 * 
	 * @param ctx
	 */
	public static void start() {
		if (!start) {
			daemon = new VisitStatService();
			click_timer = new Timer("VisitStatService", true);
			click_timer.schedule(daemon, INTERVAL, INTERVAL);// 五分钟以后运行，每隔五分钟运行一次
			start = true;
		}
		log.info("VisitStatService started.");
	}

	/**
	 * 释放Service
	 */
	public static void destroy() {
		if (start) {
			click_timer.cancel();
			daemon.cancel();
			start = false;
		}
		log.info("VisitStatService stopped.");
	}

	@Override
	public void run() {
		for (byte type : TYPES) {
			ConcurrentHashMap<Long, Integer> queue = queues.remove(type);
			queues.put(type, new ConcurrentHashMap<Long, Integer>());
			try {
				_flush(type, queue);
			} catch (Throwable t) {
				log.fatal("Failed to flush click stat data.", t);
				// SmtpHelper.reportError(null, t);
			} finally {
				queue = null;
				DBManager.closeConnection();
			}
		}
	}

	@Override
	public boolean cancel() {
		boolean b = super.cancel();
		// 写回剩余数据
		this.run();
		return b;
	}

	/**
	 * 写访问统计数据到数据库
	 * 
	 * @param type
	 * @param queue
	 */
	private void _flush(byte type, ConcurrentHashMap<Long, Integer> queue) {
		if (queue == null || queue.size() == 0)
			return;
		switch (type) {
		case VisitStat.TYPE_CODE:
			VisitStat.VisitCode(queue);
			break;
		case VisitStat.TYPE_SQUARE:
			VisitStat.VisitSquare(queue);
			break;
		}
		// System.out.printf("Flush to database: type=%d\n", type);
	}

	/**
	 * 测试
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		start();
		for (int i = 0; i < 10; i++)
			new Timer("OfferTask_" + (i + 1), false).schedule(new TimerTask() {
				private Random rnd = new Random(System.currentTimeMillis());

				@Override
				public void run() {
					record(TYPES[rnd.nextInt(TYPES.length)], rnd.nextInt(10));
				}
			}, 0, 1000);
	}

}
