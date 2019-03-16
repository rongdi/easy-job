package com.rdpaas.task.strategy;

import java.util.List;

import com.rdpaas.task.common.Node;
import com.rdpaas.task.common.Task;

/**
 * 按权重的分配策略,方案如下，假如
 * 节点序号   1     ,2     ,3       ,4
 * 节点权重   2     ,3     ,3       ,2
 * 则取余后 0,1 | 2,3,4 | 5,6,7 | 8,9
 * 序号1可以消费按照权重的和取余后小于2的
 * 序号2可以消费按照权重的和取余后大于等于2小于2+3的
 * 序号3可以消费按照权重的和取余后大于等于2+3小于2+3+3的
 * 序号3可以消费按照权重的和取余后大于等于2+3+3小于2+3+3+2的
 * 总结：本节点可以消费的按照权重的和取余后大于等于前面节点的权重和小于包括自己的权重和的这个范围
 * 想了一段时间没想到stream api一两句话搞定的方法，没时间了，先不写了，后面再补
 * @author rongdi
 * @date 2019-03-16 21:40
 */
public class WeightStrategy implements Strategy {

	@Override
	public boolean accept(List<Node> nodes, Task task, Long myNodeId) {
		
		return false;
	}

}
