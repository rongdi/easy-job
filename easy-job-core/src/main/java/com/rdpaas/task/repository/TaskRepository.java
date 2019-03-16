package com.rdpaas.task.repository;

import com.rdpaas.task.common.Invocation;
import com.rdpaas.task.common.Task;
import com.rdpaas.task.common.TaskDetail;
import com.rdpaas.task.common.TaskStatus;
import com.rdpaas.task.serializer.JdkSerializationSerializer;
import com.rdpaas.task.serializer.ObjectSerializer;
import com.rdpaas.task.utils.CronExpression;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 任务对象数据库操作对象
 * @author rongdi
 * @date 2019-03-12 19:13
 */
@Component
public class TaskRepository {

    @Autowired
    @Qualifier("easyjobJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    /**
     * 序列化工具类
     */
    private ObjectSerializer serializer = new JdkSerializationSerializer<Invocation>();

    /**
     * 列出所有待处理的非子任务
     * @param size 条数
     * @return
     */
    public List<Task> listPedding(int size) {
        String sql = "select id,node_id as nodeId,pid,`name`,cron_expr as cronExpr,status,fail_count as failCount,success_count as successCount,version,first_start_time as firstStartTime,next_start_time as nextStartTime,update_time as updateTime,create_time as createTime from easy_job_task where pid is null and next_start_time <= ? and status = 0 order by next_start_time limit ?";
        Object[] args = {new Date(),size};
        return jdbcTemplate.query(sql,args,new BeanPropertyRowMapper(Task.class));
    }

    /**
     * 查询还有指定时间开始的主任务列表
     * @param duration
     * @return
     */
    public List<Task> listPeddingTasks(int duration) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("SELECT id,node_id AS nodeId,pid,`name`,cron_expr AS cronExpr,STATUS,fail_count AS failCount,success_count AS successCount,VERSION,first_start_time AS firstStartTime,next_start_time AS nextStartTime,update_time AS updateTime,create_time AS createTime FROM easy_job_task ")
    	.append("WHERE pid IS NULL AND TIMESTAMPDIFF(SECOND,NOW(),next_start_time) < ? AND STATUS = 0 ")
    	.append("ORDER BY next_start_time");
    	Object[] args = {duration};
        return jdbcTemplate.query(sb.toString(),args,new BeanPropertyRowMapper(Task.class));
    }
    
    /**
     * 列出指定任务的任务详情
     * @param taskId 任务id
     * @return
     */
    public List<TaskDetail> listDetails(Long taskId) {
        String sql = "select id,node_id as nodeId,task_id as taskId,retry_count as retryCount,start_time as startTime,end_time as endTime,type,`status`,error_msg as errorMsg from easy_job_task_detail where task_id = ?";
        Object[] args = {taskId};
        return jdbcTemplate.query(sql,args,new BeanPropertyRowMapper(TaskDetail.class));
    }

    /**
     * 获取指定任务的子任务明细列表
     * @param id
     * @return
     */
    public List<TaskDetail> getDetailChilds(Long id) {
        String sql = "select id,pid,task_id as taskId,node_id as nodeId,retry_count as retryCount,version,status,start_time as startTime,end_time as endTime from easy_job_task_detail where pid = ?";
        Object[] args = {id};
        List<TaskDetail> tasks = jdbcTemplate.query(sql,args,new BeanPropertyRowMapper(TaskDetail.class));
        if(tasks == null) {
            return null;
        }
        return tasks;
    }

    /**
     * 列出需要恢复的任务，需要恢复的任务是指所属执行节点已经挂了并且该任务还属于执行中的任务
     * @param timeout 超时时间
     * @return
     */
    public List<Task> listRecoverTasks(int timeout) {
        StringBuilder sb = new StringBuilder();
        sb.append("select t.* from easy_job_task t left join easy_job_node n on t.node_id = n.id ")
                .append("where t.status = 1 and timestampdiff(SECOND,n.update_time,now()) > ?");
        Object[] args = {timeout};
        return jdbcTemplate.query(sb.toString(),args,new BeanPropertyRowMapper(Task.class));
    }

    /**
     * 根据指定id获取具体任务对象
     * @param id
     * @return
     */
    public Task get(Long id) {
        String sql = "select id,node_id as nodeId,pid,`name`,cron_expr as cronExpr,status,fail_count as failCount,success_count as successCount,version,first_start_time as firstStartTime,next_start_time as nextStartTime,update_time as updateTime,create_time as createTime,invoke_info as invokeInfo from easy_job_task where id = ?";
        Object[] args = {id};
        Task task = (Task)jdbcTemplate.queryForObject(sql,new BeanPropertyRowMapper(Task.class),args);
        if(task != null) {
            task.setInvokor((Invocation) serializer.deserialize(task.getInvokeInfo()));
        }
        return task;
    }
    
    /**
     * 根据指定id获取具体任务明细对象
     * @param id
     * @return
     */
    public TaskDetail getDetail(Long id) {
    	 String sql = "select id,node_id as nodeId,task_id as taskId,retry_count as retryCount,start_time as startTime,end_time as endTime,type,`status`,error_msg as errorMsg from easy_job_task_detail where id = ?";
         Object[] args = {id};
         return (TaskDetail)jdbcTemplate.queryForObject(sql,new BeanPropertyRowMapper(TaskDetail.class),args);
    }

    /**
     * 获取指定任务的子任务列表
     * @param id
     * @return
     */
    public List<Task> getChilds(Long id) {
        String sql = "select id,node_id as nodeId,pid,`name`,cron_expr as cronExpr,status,fail_count as failCount,success_count as successCount,version,first_start_time as firstStartTime,next_start_time as nextStartTime,update_time as updateTime,create_time as createTime from easy_job_task where pid = ?";
        Object[] args = {id};
        List<Task> tasks = jdbcTemplate.query(sql,args,new BeanPropertyRowMapper(Task.class));
        if(tasks == null) {
            return null;
        }
        for(Task task:tasks) {
            task.setInvokor((Invocation) serializer.deserialize(task.getInvokeInfo()));
        }
        return tasks;
    }

    /**
     * 查找在当前明细之后是否还有相同状态的同属一一个主任务对象的明细
     * @param detail
     * @return
     */
    public Long findNextId(Long taskId,Long id,TaskStatus status) {
        String sql = "SELECT id FROM `easy_job_task_detail` WHERE task_id = ? AND id > ? and status = ? LIMIT 1";
        Object[] args = {taskId,id,status.getId()};
        return jdbcTemplate.queryForObject(sql,args,Long.class);
    }
    
    /**
     * 插入任务
     * @param task 待插入任务
     * @return
     * @throws Exception
     */
    public long insert(Task task) throws Exception {
        CronExpression cronExpession = new CronExpression(task.getCronExpr());
        Date nextStartDate = cronExpession.getNextValidTimeAfter(new Date());
        task.setFirstStartTime(nextStartDate);
        task.setNextStartTime(nextStartDate);
        String sql = "INSERT INTO easy_job_task(`name`,pid,cron_expr,status,create_time,update_time,first_start_time,next_start_time,invoke_info) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con)
                    throws SQLException {
                //设置返回的主键字段名
                PreparedStatement ps = con.prepareStatement(sql,new String[]{"id"});
                ps.setString(1,task.getName());
                ps.setObject(2,task.getPid());
                ps.setString(3,task.getCronExpr());
                ps.setInt(4,task.getStatus().getId());
                ps.setTimestamp(5,new java.sql.Timestamp(task.getCreateTime().getTime()));
                ps.setTimestamp(6,new java.sql.Timestamp(task.getUpdateTime().getTime()));
                ps.setTimestamp(7,task.getFirstStartTime() == null?null:new java.sql.Timestamp(task.getFirstStartTime().getTime()));
                ps.setTimestamp(8,task.getNextStartTime() == null?null:new java.sql.Timestamp(task.getNextStartTime().getTime()));
                ps.setBytes(9, serializer.serialize(task.getInvokor()));
                return ps;
            }
        }, kh);
        return kh.getKey().longValue();
    }

    /**
     * 插入任务详情
     * @param taskDetail 待插入任务详情
     * @return
     */
    public long insert(TaskDetail taskDetail) {
        String sql = "INSERT INTO easy_job_task_detail(`task_id`,pid,start_time,status,node_id) VALUES (?, ?, ?, ?,?);";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con)
                    throws SQLException {
                //设置返回的主键字段名
                PreparedStatement ps = con.prepareStatement(sql,new String[]{"id"});
                ps.setLong(1,taskDetail.getTaskId());
                ps.setObject(2,taskDetail.getPid());
                ps.setTimestamp(3, new java.sql.Timestamp(taskDetail.getStartTime().getTime()));
                ps.setInt(4,taskDetail.getStatus().getId());
                ps.setLong(5,taskDetail.getNodeId());
                return ps;
            }
        }, kh);

        return kh.getKey().longValue();
    }

    /**
     * 开始一个任务
     * @param task 待开始的任务
     * @return
     * @throws Exception
     */
    public TaskDetail start(Task task) throws Exception {
        TaskDetail taskDetail = new TaskDetail(task.getId());
        taskDetail.setNodeId(task.getNodeId());
        long id = insert(taskDetail);
        taskDetail.setId(id);
        return taskDetail;
    }

    /**
     * 开始一个子任务
     * @param task 待开始的任务
     * @return
     * @throws Exception
     */
    public TaskDetail startChild(Task task,TaskDetail detail) throws Exception {
        TaskDetail taskDetail = new TaskDetail(task.getId());
        taskDetail.setNodeId(task.getNodeId());
        taskDetail.setPid(detail.getId());
        long id = insert(taskDetail);
        taskDetail.setId(id);
        return taskDetail;
    }

    /**
     * 完成任务
     * @param task 待开始的任务
     * @param detail 本次执行的具体任务详情
     * @throws Exception
     */
    public void finish(Task task,TaskDetail detail) throws Exception {
        CronExpression cronExpession = new CronExpression(task.getCronExpr());
        Date nextStartDate = cronExpession.getNextValidTimeAfter(task.getNextStartTime());
        /**
         *  如果没有下次执行时间了，该任务就完成了，反之变成待执行
         */
        if(nextStartDate == null) {
            task.setStatus(TaskStatus.FINISH);
        } else {
            task.setStatus(TaskStatus.PENDING);
        }
        /**
         * 增加任务成功次数
         */
        task.setSuccessCount(task.getSuccessCount() + 1);
        task.setNextStartTime(nextStartDate);
        /**
         * 使用乐观锁检测是否可以更新成功，成功则更新详情
         */
        int n = updateWithVersion(task);
        if(n > 0) {
            detail.setEndTime(new Date());
            detail.setStatus(TaskStatus.FINISH);
            update(detail);
            addCounts(detail.getNodeId());
        }
    }

    /**
     * 记录任务失败信息
     * @param task 待失败任务
     * @param detail 任务详情
     * @param errorMsg 出错信息
     * @throws Exception
     */
    public void fail(Task task,TaskDetail detail,String errorMsg) throws Exception {
        if(detail == null) return;
        //如果没有下次执行时间了，该任务就完成了，反之变成待执行
        task.setStatus(TaskStatus.ERROR);
        task.setFailCount(task.getFailCount() + 1);
        int n = updateWithVersion(task);
        if(n > 0) {
            detail.setEndTime(new Date());
            detail.setStatus(TaskStatus.ERROR);
            detail.setErrorMsg(errorMsg);
            update(detail);
            addCounts(detail.getNodeId());
        }
    }

    /**
     * 使用乐观锁更新任务
     * @param task 待更新任务
     * @return
     * @throws Exception
     */
    public int updateWithVersion(Task task) throws Exception  {
        StringBuilder sb = new StringBuilder();
        List<Object> objs = new ArrayList<>();
        sb.append("update easy_job_task set version = version + 1,next_start_time = ?");
        objs.add(task.getNextStartTime());
        if(task.getStatus() != null) {
            sb.append(",status = ? ");
            objs.add(task.getStatus().getId());
        }
        if(task.getUpdateTime() != null) {
            sb.append(",update_time = ? ");
            objs.add(task.getUpdateTime());
        }
        if(task.getSuccessCount() != null) {
            sb.append(",success_count = ? ");
            objs.add(task.getSuccessCount());
        }
        if(task.getFailCount() != null) {
            sb.append(",fail_count = ? ");
            objs.add(task.getFailCount());
        }
        if(task.getNodeId() != null) {
            sb.append(",node_id = ? ");
            objs.add(task.getNodeId());
        }
        sb.append("where version = ? and id = ?");
        objs.add(task.getVersion());
        objs.add(task.getId());
        return jdbcTemplate.update(sb.toString(), objs.toArray());
    }

    /**
     * 不使用乐观锁更新任务，比如拿到任务后只是临时更新一下状态
     * @param task 待更新任务
     * @return
     * @throws Exception
     */
    public int update(Task task) throws Exception  {
        StringBuilder sb = new StringBuilder();
        List<Object> objs = new ArrayList<>();
        sb.append("update easy_job_task set next_start_time = ?");
        objs.add(task.getNextStartTime());
        if(task.getStatus() != null) {
            sb.append(",status = ? ");
            objs.add(task.getStatus().getId());
        }
        if(task.getUpdateTime() != null) {
            sb.append(",update_time = ? ");
            objs.add(task.getUpdateTime());
        }
        if(task.getSuccessCount() != null) {
            sb.append(",success_count = ? ");
            objs.add(task.getSuccessCount());
        }
        if(task.getFailCount() != null) {
            sb.append(",fail_count = ? ");
            objs.add(task.getFailCount());
        }
        if(task.getNodeId() != null) {
            sb.append(",node_id = ? ");
            objs.add(task.getNodeId());
        }
        sb.append("where version = ? and id = ?");
        objs.add(task.getVersion());
        objs.add(task.getId());
        return jdbcTemplate.update(sb.toString(), objs.toArray());
    }

    /**
     * 更新任务详情，这里没有使用乐观锁，是因为同一个任务详情同一个时间点只会在一个节点执行，
     * 因为需要根据乐观锁先更新了任务的状态的节点才能执行详情操作,这个方法主要给任务调度用
     * @param taskDetail
     * @return
     * @throws Exception
     */
    public int update(TaskDetail taskDetail) throws Exception  {

        StringBuilder sb = new StringBuilder();
        List<Object> objs = new ArrayList<>();
        sb.append("update easy_job_task_detail set status = ?");
        objs.add(taskDetail.getStatus().getId());
        if(taskDetail.getRetryCount() != null) {
            sb.append(",retry_count = ? ");
            objs.add(taskDetail.getRetryCount());
        }
        if(taskDetail.getEndTime() != null) {
            sb.append(",end_time = ? ");
            objs.add(taskDetail.getEndTime());
        }
        if(taskDetail.getStatus() != null) {
            sb.append(",status = ? ");
            objs.add(taskDetail.getStatus().getId());
        }
        if(taskDetail.getErrorMsg() != null) {
            sb.append(",error_msg = ? ");
            objs.add(taskDetail.getErrorMsg());
        }
        if(taskDetail.getNodeId() != null) {
            sb.append(",node_id = ? ");
            objs.add(taskDetail.getNodeId());
        }
        sb.append("where id = ?");
        objs.add(taskDetail.getId());
        return jdbcTemplate.update(sb.toString(), objs.toArray());
    }

    /**
     * 更新任务详情，这里使用乐观锁，是因为多个节点的恢复线程可能会产生竞争
     * @param taskDetail
     * @return
     * @throws Exception
     */
    public int updateWithVersion(TaskDetail taskDetail) throws Exception  {

        StringBuilder sb = new StringBuilder();
        List<Object> objs = new ArrayList<>();
        sb.append("update easy_job_task_detail set version = version + 1");
        objs.add(taskDetail.getStatus().getId());
        if(taskDetail.getRetryCount() != null) {
            sb.append(",retry_count = ? ");
            objs.add(taskDetail.getRetryCount());
        }
        if(taskDetail.getEndTime() != null) {
            sb.append(",end_time = ? ");
            objs.add(taskDetail.getEndTime());
        }
        if(taskDetail.getStatus() != null) {
            sb.append(",status = ? ");
            objs.add(taskDetail.getStatus().getId());
        }
        if(taskDetail.getErrorMsg() != null) {
            sb.append(",error_msg = ? ");
            objs.add(taskDetail.getErrorMsg());
        }
        if(taskDetail.getNodeId() != null) {
            sb.append(",node_id = ? ");
            objs.add(taskDetail.getNodeId());
        }
        sb.append("where id = ? and version = ? ");
        objs.add(taskDetail.getId());
        objs.add(taskDetail.getVersion());
        return jdbcTemplate.update(sb.toString(), objs.toArray());
    }

    /**
     * 给节点增加处理次数
     * @param nodeId
     * @return
     */
    public int addCounts(Long nodeId) {
        StringBuilder sb = new StringBuilder();
        sb.append("update easy_job_node set counts = counts + 1 ")
                .append("where node_id = ?");
        Object objs[] = {nodeId};
        return jdbcTemplate.update(sb.toString(), objs);
    }
}
