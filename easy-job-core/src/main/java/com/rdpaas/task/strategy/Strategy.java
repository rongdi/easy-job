package com.rdpaas.task.strategy;

import java.util.List;

import com.rdpaas.task.common.Node;
import com.rdpaas.task.common.Task;

/**
 * 抽象的策略接口
 * @author rongdi
 * @date 2019-03-16 12:36
 */
public interface Strategy {

	/**
	 * 默认策略
	 */
	String DEFAULT = "default";
	
	public static Strategy choose(String key) {
		switch(key) {
			default:
				return new DefaultStrategy();
		}
	}
	
	public boolean accept(List<Node> nodes,Task task,Long myNodeId);
	
}
