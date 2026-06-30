package cc.oofo.system.job.quartz;

import java.lang.reflect.Method;
import java.sql.Timestamp;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import cc.oofo.system.job.entity.SysJob;
import cc.oofo.system.job.entity.SysJobLog;
import cc.oofo.system.job.mapper.SysJobLogMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务执行器：允许并发版。
 *
 * 通过反射调用 Spring Bean 的方法，并记录执行日志。
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Slf4j
public class QuartzJobExecutor implements Job {

    public static final String JOB_KEY = "SYS_JOB_ENTITY";

    @Override
    public void execute(JobExecutionContext context) {
        SysJob job = (SysJob) context.getMergedJobDataMap().get(JOB_KEY);
        if (job == null) {
            log.warn("Quartz 任务缺少 SYS_JOB_ENTITY 数据，已跳过");
            return;
        }
        invokeAndLog(job);
    }

    /** 反射调用 + 写日志 */
    public static void invokeAndLog(SysJob job) {
        long start = System.currentTimeMillis();
        SysJobLog logEntity = new SysJobLog();
        logEntity.setJobId(job.getId());
        logEntity.setJobName(job.getJobName());
        logEntity.setJobGroup(job.getJobGroup());
        logEntity.setBeanName(job.getBeanName());
        logEntity.setMethodName(job.getMethodName());
        logEntity.setMethodParams(job.getMethodParams());
        logEntity.setStatus(2);
        logEntity.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        SysJobLogMapper logMapper = SpringContextHolder.getBean(SysJobLogMapper.class);
        boolean logInserted = false;
        try {
            logMapper.insert(logEntity);
            logInserted = true;
        } catch (Exception ex) {
            log.error("写入定时任务执行中日志失败", ex);
        }

        try {
            doInvoke(job);
            logEntity.setStatus(1);
        } catch (Throwable e) {
            log.error("定时任务执行失败 [{}.{}]", job.getBeanName(), job.getMethodName(), e);
            logEntity.setStatus(0);
            logEntity.setErrorMsg(rootCauseMessage(e));
        } finally {
            logEntity.setCostTime(System.currentTimeMillis() - start);
            try {
                if (logInserted) {
                    logMapper.updateById(logEntity);
                } else {
                    logMapper.insert(logEntity);
                }
            } catch (Exception ex) {
                log.error("更新定时任务日志失败", ex);
            }
        }
    }

    private static void doInvoke(SysJob job) throws Exception {
        if (!StringUtils.hasText(job.getBeanName())) {
            throw new IllegalArgumentException("beanName 不能为空");
        }
        ApplicationContext ctx = SpringContextHolder.getApplicationContext();
        Object bean = ctx.getBean(job.getBeanName());
        String methodName = StringUtils.hasText(job.getMethodName())
                ? job.getMethodName() : "execute";

        Method method;
        if (StringUtils.hasText(job.getMethodParams())) {
            method = bean.getClass().getMethod(methodName, String.class);
            method.invoke(bean, job.getMethodParams());
        } else {
            try {
                method = bean.getClass().getMethod(methodName);
                method.invoke(bean);
            } catch (NoSuchMethodException ignored) {
                method = bean.getClass().getMethod(methodName, String.class);
                method.invoke(bean, (String) null);
            }
        }
    }

    private static String rootCauseMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) t = t.getCause();
        return t.getClass().getSimpleName() + ": " + t.getMessage();
    }
}
