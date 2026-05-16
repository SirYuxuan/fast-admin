package cc.oofo.system.job.service;

import org.quartz.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;
import cc.oofo.system.job.entity.SysJob;
import cc.oofo.system.job.entity.query.SysJobQuery;
import lombok.RequiredArgsConstructor;

/**
 * 定时任务服务
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SysJobService extends BaseService<SysJob> {

    private final JobScheduleService scheduleService;

    public Page<SysJob> page(SysJobQuery query) {
        query.getQueryWrapper().orderByDesc("created_at");
        return page(query.getMPPage(), query.getQueryWrapper());
    }

    public void add(SysJob job) {
        validate(job, null);
        defaults(job);
        save(job);
        scheduleService.createJob(job);
    }

    public void edit(SysJob job) {
        if (!StringUtils.hasText(job.getId())) {
            throw new BizException("任务ID不能为空");
        }
        validate(job, job.getId());
        defaults(job);
        updateById(job);
        scheduleService.updateJob(job);
    }

    public void del(String id) {
        SysJob job = getById(id);
        if (job == null) return;
        scheduleService.deleteJob(job);
        removeById(id);
    }

    /** 修改状态：1启动 0暂停 */
    public void changeStatus(String id, Integer status) {
        SysJob job = getById(id);
        if (job == null) throw new BizException("任务不存在");
        job.setStatus(status);
        updateById(job);
        if (status != null && status == 1) {
            scheduleService.resumeJob(job);
        } else {
            scheduleService.pauseJob(job);
        }
    }

    /** 立即执行一次 */
    public void runOnce(String id) {
        SysJob job = getById(id);
        if (job == null) throw new BizException("任务不存在");
        scheduleService.runOnce(job);
    }

    private void validate(SysJob job, String excludeId) {
        if (!StringUtils.hasText(job.getJobName())) throw new BizException("任务名称不能为空");
        if (!StringUtils.hasText(job.getBeanName())) throw new BizException("Bean 名不能为空");
        if (!StringUtils.hasText(job.getCronExpression())) throw new BizException("Cron 表达式不能为空");
        if (!CronExpression.isValidExpression(job.getCronExpression())) {
            throw new BizException("Cron 表达式不合法：" + job.getCronExpression());
        }
        boolean dup = query()
                .ne(StringUtils.hasText(excludeId), "id", excludeId)
                .eq("job_name", job.getJobName())
                .eq("job_group", job.getJobGroup() == null ? "DEFAULT" : job.getJobGroup())
                .exists();
        if (dup) throw new BizException("任务名称已存在");
    }

    private void defaults(SysJob job) {
        if (!StringUtils.hasText(job.getJobGroup())) job.setJobGroup("DEFAULT");
        if (!StringUtils.hasText(job.getMethodName())) job.setMethodName("execute");
        if (job.getMisfirePolicy() == null) job.setMisfirePolicy(2);
        if (job.getConcurrent() == null) job.setConcurrent(0);
        if (job.getStatus() == null) job.setStatus(0);
    }
}
