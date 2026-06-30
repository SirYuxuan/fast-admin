package cc.oofo.flow.cc.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cc.oofo.flow.cc.entity.FlowCcRecord;
import cc.oofo.flow.cc.mapper.FlowCcRecordMapper;
import cc.oofo.flow.common.FlowIdentity;
import lombok.RequiredArgsConstructor;

/**
 * 抄送服务：我的抄送列表、标记已读、未读数。
 *
 * @author Sir丶雨轩
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FlowCcService {

    private final FlowCcRecordMapper ccMapper;
    private final FlowIdentity identity;

    public Page<FlowCcRecord> myCc(int page, int pageSize) {
        return ccMapper.selectPage(new Page<>(page, pageSize),
                new QueryWrapper<FlowCcRecord>()
                        .eq("cc_user_id", identity.userId())
                        .orderByDesc("created_at"));
    }

    public long unread() {
        return ccMapper.selectCount(new QueryWrapper<FlowCcRecord>()
                .eq("cc_user_id", identity.userId())
                .eq("is_read", 0));
    }

    public void markRead(String id) {
        FlowCcRecord cc = ccMapper.selectById(id);
        if (cc != null && Integer.valueOf(0).equals(cc.getIsRead())) {
            FlowCcRecord update = new FlowCcRecord();
            update.setId(id);
            update.setIsRead(1);
            ccMapper.updateById(update);
        }
    }
}
