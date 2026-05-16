package cc.oofo.system.job.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

/**
 * 任务执行器：不允许并发版（前一次未结束时，后一次会等待）。
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@DisallowConcurrentExecution
public class QuartzDisallowConcurrentExecution extends QuartzJobExecutor {

    @Override
    public void execute(JobExecutionContext context) {
        super.execute(context);
    }
}
