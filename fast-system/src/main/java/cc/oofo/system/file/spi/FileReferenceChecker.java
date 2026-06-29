package cc.oofo.system.file.spi;

import java.util.Optional;

/**
 * 文件引用检查扩展点。
 *
 * <p>其它模块（如 AI 知识库）若引用了系统文件，可实现本接口并注册为 Spring Bean，
 * 删除文件前会逐个调用，任一返回非空原因则禁止删除。</p>
 *
 * <p>该接口定义在 fast-system，业务模块（fast-ai 等）依赖 fast-system 实现它，
 * 避免 fast-system 反向依赖业务模块造成循环依赖。</p>
 *
 * @author Sir丶雨轩
 */
public interface FileReferenceChecker {

    /**
     * 检查文件是否被引用。
     *
     * @param fileId 系统文件 ID
     * @return 被引用时返回禁止删除的原因；未被引用返回 {@link Optional#empty()}
     */
    Optional<String> checkReference(String fileId);
}
