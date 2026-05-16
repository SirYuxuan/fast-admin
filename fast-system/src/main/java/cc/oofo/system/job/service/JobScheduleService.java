package cc.oofo.system.job.service;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

import cc.oofo.framework.exception.BizException;
import cc.oofo.system.job.entity.SysJob;
import cc.oofo.system.job.quartz.QuartzDisallowConcurrentExecution;
import cc.oofo.system.job.quartz.QuartzJobExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务调度服务：封装 Quartz Scheduler 操作。
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobScheduleService {

    private final Scheduler scheduler;

    /** 将任务添加到 Quartz */
    public void createJob(SysJob job) {
        try {
            JobDetail detail = JobBuilder
                    .newJob(getJobClass(job))
                    .withIdentity(buildJobKey(job))
                    .build();
            detail.getJobDataMap().put(QuartzJobExecutor.JOB_KEY, job);

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(buildTriggerKey(job))
                    .withSchedule(applyMisfire(
                            CronScheduleBuilder.cronSchedule(job.getCronExpression()),
                            job.getMisfirePolicy()))
                    .build();

            // 若已存在则先删后增
            JobKey jobKey = buildJobKey(job);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }

            scheduler.scheduleJob(detail, trigger);

            // 默认状态：根据 status 决定是否暂停
            if (job.getStatus() != null && job.getStatus() == 0) {
                scheduler.pauseJob(jobKey);
            }
        } catch (Exception e) {
            log.error("createJob fail", e);
            throw new BizException("创建调度任务失败：" + e.getMessage());
        }
    }

    /** 更新（删除后重新创建即可，简单稳定） */
    public void updateJob(SysJob job) {
        deleteJob(job);
        createJob(job);
    }

    /** 删除 */
    public void deleteJob(SysJob job) {
        try {
            JobKey key = buildJobKey(job);
            if (scheduler.checkExists(key)) {
                scheduler.deleteJob(key);
            }
        } catch (SchedulerException e) {
            log.error("deleteJob fail", e);
            throw new BizException("删除调度任务失败：" + e.getMessage());
        }
    }

    /** 暂停 */
    public void pauseJob(SysJob job) {
        try {
            scheduler.pauseJob(buildJobKey(job));
        } catch (SchedulerException e) {
            throw new BizException("暂停任务失败：" + e.getMessage());
        }
    }

    /** 恢复 */
    public void resumeJob(SysJob job) {
        try {
            scheduler.resumeJob(buildJobKey(job));
        } catch (SchedulerException e) {
            throw new BizException("恢复任务失败：" + e.getMessage());
        }
    }

    /** 立即执行一次（不影响原有 cron） */
    public void runOnce(SysJob job) {
        try {
            JobKey key = buildJobKey(job);
            if (!scheduler.checkExists(key)) {
                // 没有调度记录时，临时构造 JobDetail 直接触发
                createJob(job);
            }
            // 通过 triggerJob 立即触发
            org.quartz.JobDataMap data = new org.quartz.JobDataMap();
            data.put(QuartzJobExecutor.JOB_KEY, job);
            scheduler.triggerJob(key, data);
        } catch (SchedulerException e) {
            throw new BizException("立即执行失败：" + e.getMessage());
        }
    }

    // ------------------------ helpers ------------------------

    private JobKey buildJobKey(SysJob job) {
        return JobKey.jobKey("JOB_" + job.getId(),
                job.getJobGroup() == null ? "DEFAULT" : job.getJobGroup());
    }

    private TriggerKey buildTriggerKey(SysJob job) {
        return TriggerKey.triggerKey("TRIGGER_" + job.getId(),
                job.getJobGroup() == null ? "DEFAULT" : job.getJobGroup());
    }

    private Class<? extends org.quartz.Job> getJobClass(SysJob job) {
        return (job.getConcurrent() != null && job.getConcurrent() == 1)
                ? QuartzJobExecutor.class
                : QuartzDisallowConcurrentExecution.class;
    }

    private CronScheduleBuilder applyMisfire(CronScheduleBuilder cb, Integer policy) {
        if (policy == null) return cb.withMisfireHandlingInstructionDoNothing();
        return switch (policy) {
            case 1 -> cb.withMisfireHandlingInstructionFireAndProceed();
            case 3 -> cb.withMisfireHandlingInstructionIgnoreMisfires();
            default -> cb.withMisfireHandlingInstructionDoNothing();
        };
    }
}
