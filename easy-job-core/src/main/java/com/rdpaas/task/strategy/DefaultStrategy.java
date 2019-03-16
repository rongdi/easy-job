package com.rdpaas.task.strategy;

import java.util.List;

import com.rdpaas.task.common.Node;
import com.rdpaas.task.common.Task;

/**
 * 默认的来者不惧的策略，只要能抢到就要，其实这种方式等同于随机分配任务，因为谁可以抢到不一定
 * @author rongdi
 * @date 2019-03-16 21:34
 */
public class DefaultStrategy implements Strategy {

	@Override
	public boolean accept(List<Node> nodes, Task task, Long myNodeId) {
		return true;
	}

}
