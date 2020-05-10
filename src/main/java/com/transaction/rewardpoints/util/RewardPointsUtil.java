package com.transaction.rewardpoints.util;

import com.transaction.rewardpoints.consts.RewardPointsConsts;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class RewardPointsUtil {

    public static List<Map<String, Object>> calculate(List<Map<String, Object>> transactions) {

        if (verifyRequiredPayload(transactions)) {
            return transactions;
        }

        // calculate reward points for each transaction. Rounds to nearest integer
        for (Map<String, Object> transaction : transactions) {
            final int transactionAmount = (int) Math.round(Double.valueOf(transaction.get(RewardPointsConsts.TRANSACTION_AMOUNT).toString()));
            final int rewardPoints =
                    (((2 * Math.max(0, transactionAmount - 100)) +
                     (Math.max(50, transactionAmount) == Math.min(transactionAmount, 99) ? Math.max(0, transactionAmount - 50) : 0)));
            transaction.put((RewardPointsConsts.REWARD_POINTS), rewardPoints);
        }

        // groups transactions by customerID
        final Map<String, List<Map<String, Object>>> resultsCustomerIDGrouping =
                transactions.stream().collect(
                        Collectors.groupingBy(map -> map.get(RewardPointsConsts.CUSTOMER_ID).toString(),
                        Collectors.toList()));

        // group customer transactions by transactionDate MONTH and sum all reward points for that month.
        List<Map<String, Object>> rewardPointsResponse = new ArrayList<>();
        for (Map.Entry entry : resultsCustomerIDGrouping.entrySet()) {
            Map<String, Object> customer = new HashMap<>();
            customer.put((RewardPointsConsts.CUSTOMER_ID), entry.getKey());

            final Collection<Map<String, Object>> values = (Collection<Map<String, Object>>) entry.getValue();

            final Map<String, Integer> rewardPoints = values.stream().collect(Collectors.groupingBy(m -> Month.from(LocalDate.parse((String) m.get(RewardPointsConsts.TRANSACTION_DATE))).name(),
                    TreeMap::new,
                    Collectors.summingInt(m -> (Integer) m.get(RewardPointsConsts.REWARD_POINTS))));

            rewardPoints.put("total", rewardPoints.values().stream().reduce(0, Integer::sum));
            customer.put(RewardPointsConsts.REWARD_POINTS, rewardPoints);

            rewardPointsResponse.add(customer);
        }

        return rewardPointsResponse;
    }

    private static boolean verifyRequiredPayload(List<Map<String, Object>> transactions) {
        boolean hasError = false;
        for (Map<String, Object> transaction : transactions) {
            List<String> errorMessages = new ArrayList<>();
            if (!transaction.containsKey(RewardPointsConsts.CUSTOMER_ID)) {
                errorMessages.add("Missing required field: " + RewardPointsConsts.CUSTOMER_ID);
                hasError = true;
            }

            if (!transaction.containsKey(RewardPointsConsts.TRANSACTION_AMOUNT)) {
                errorMessages.add("Missing required field: " + RewardPointsConsts.TRANSACTION_AMOUNT);
                hasError = true;
            }

            if (!transaction.containsKey(RewardPointsConsts.TRANSACTION_DATE)) {
                errorMessages.add("Missing required field: " + RewardPointsConsts.TRANSACTION_DATE);
                hasError = true;
            }

            if (!CollectionUtils.isEmpty(errorMessages)) {
                transaction.put("Errors", errorMessages);
            }
        }
        return hasError;
    }
}
