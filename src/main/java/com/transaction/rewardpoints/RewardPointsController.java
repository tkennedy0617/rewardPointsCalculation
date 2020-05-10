package com.transaction.rewardpoints;

import com.transaction.rewardpoints.consts.RewardPointsConsts;
import com.transaction.rewardpoints.util.RewardPointsUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
public class RewardPointsController {

    @PostMapping(path = "/calculate")
    public List<Map<String, Object>> calculate(@RequestBody Map<String, Object> transactions) {
        if (CollectionUtils.isEmpty(transactions) ||
                (transactions.containsKey(RewardPointsConsts.TRANSACTIONS) && (CollectionUtils.isEmpty((Collection<?>) transactions.get(RewardPointsConsts.TRANSACTIONS))))) {
            throw new IllegalArgumentException("No transactions to process");
        }

        return RewardPointsUtil.calculate((List<Map<String, Object>>) transactions.get(RewardPointsConsts.TRANSACTIONS));
    }
}
