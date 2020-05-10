package com.transaction.rewardpoints.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RewardPointsUtilTests {

    @DataProvider(name = "verifyPayloadData")
    public Object[][] verifyPayloadData() {
        return new Object[][] {
                {
                        Arrays.asList(Stream.of(new Object[][]{
                                {"customerID", 111},
                                {"transactionAmount", 54},
                        }).collect(Collectors.toMap(data -> data[0], data -> data[1]))),
                        Arrays.asList("Missing required field: transactionDate")
                },
                {
                        Arrays.asList(Stream.of(new Object[][]{
                                {"transactionAmount", 54},
                                {"transactionDate", "2019-01-01"}
                        }).collect(Collectors.toMap(data -> data[0], data -> data[1]))),
                        Arrays.asList("Missing required field: customerID")
                },
                {
                        Arrays.asList(Stream.of(new Object[][]{
                                {"customerID", 111},
                                {"transactionDate", "2019-01-01"},
                        }).collect(Collectors.toMap(data -> data[0], data -> data[1]))),
                        Arrays.asList("Missing required field: transactionAmount")
                },
                {
                        Arrays.asList(Stream.of(new Object[][]{
                                {"customerID", 111},
                        }).collect(Collectors.toMap(data -> data[0], data -> data[1]))),
                        Arrays.asList("Missing required field: transactionDate", "Missing required field: transactionAmount")
                },
                {
                        Arrays.asList(Stream.of(new Object[][]{
                                {"transactionAmount", 54},
                        }).collect(Collectors.toMap(data -> data[0], data -> data[1]))),
                        Arrays.asList("Missing required field: customerID", "Missing required field: transactionDate")
                },
                {
                        Arrays.asList(Stream.of(new Object[][]{
                                {"transactionDate", "2019-01-01"},
                        }).collect(Collectors.toMap(data -> data[0], data -> data[1]))),
                        Arrays.asList("Missing required field: customerID", "Missing required field: transactionAmount")
                }
        };
    }

    @Test(dataProvider = "verifyPayloadData")
    public void testCalculateVerifyPayload(List<Map<String, Object>> transactions, List<String> expectedErrorMessages) {

        final List<Map<String, Object>> actualResults = RewardPointsUtil.calculate(transactions);

        Assert.assertTrue(actualResults.get(0).containsKey("Errors"));

        final List<String> errorMessages = (List<String>) actualResults.get(0).get("Errors");

        for (String errorMessage : errorMessages) {
            Assert.assertTrue(expectedErrorMessages.contains(errorMessage));
        }
    }

    @Test
    public void testHappyPathRewardPointsCalculationOneCustomerTwoMonths() {

        Map<String, Object> customer1Transaction1 = new HashMap<>();
        customer1Transaction1.put("customerID", 111);
        customer1Transaction1.put("transactionDate", "2020-02-14");
        customer1Transaction1.put("transactionAmount", 230.10);

        Map<String, Object> customer1Transaction2 = new HashMap<>();
        customer1Transaction2.put("customerID", 111);
        customer1Transaction2.put("transactionDate", "2020-03-14");
        customer1Transaction2.put("transactionAmount", 57);

        final List<Map<String, Object>> actualResults = RewardPointsUtil.calculate(Arrays.asList(customer1Transaction1, customer1Transaction2));

        Assert.assertEquals(actualResults.size(), 1);

        Assert.assertEquals(actualResults.get(0).get("customerID"), 111);
        Assert.assertEquals(((Map) actualResults.get(0).get("rewardPoints")).get("FEBRUARY"), 260);
        Assert.assertEquals(((Map) actualResults.get(0).get("rewardPoints")).get("MARCH"), 7);
        Assert.assertEquals(((Map) actualResults.get(0).get("rewardPoints")).get("total"), 267);
    }

    @Test
    public void testHappyPathRewardPointsCalculationTwoCustomers() {

        Map<String, Object> customer1Transaction1 = new HashMap<>();
        customer1Transaction1.put("customerID", 111);
        customer1Transaction1.put("transactionDate", "2020-02-14");
        customer1Transaction1.put("transactionAmount", 230.10);

        Map<String, Object> customer1Transaction2 = new HashMap<>();
        customer1Transaction2.put("customerID", 111);
        customer1Transaction2.put("transactionDate", "2020-03-14");
        customer1Transaction2.put("transactionAmount", 57);

        Map<String, Object> customer2Transaction1 = new HashMap<>();
        customer2Transaction1.put("customerID", 222);
        customer2Transaction1.put("transactionDate", "2020-02-14");
        customer2Transaction1.put("transactionAmount", 20);

        Map<String, Object> customer2Transaction2 = new HashMap<>();
        customer2Transaction2.put("customerID", 222);
        customer2Transaction2.put("transactionDate", "2020-02-14");
        customer2Transaction2.put("transactionAmount", 99);

        final List<Map<String, Object>> actualResults = RewardPointsUtil.calculate(Arrays.asList(customer1Transaction1, customer1Transaction2, customer2Transaction1, customer2Transaction2));

        Assert.assertEquals(actualResults.size(), 2);

        Assert.assertEquals(actualResults.get(0).get("customerID"), 222);
        Assert.assertEquals(((Map) actualResults.get(0).get("rewardPoints")).get("FEBRUARY"), 49);
        Assert.assertEquals(((Map) actualResults.get(0).get("rewardPoints")).get("total"), 49);

        Assert.assertEquals(actualResults.get(1).get("customerID"), 111);
        Assert.assertEquals(((Map) actualResults.get(1).get("rewardPoints")).get("FEBRUARY"), 260);
        Assert.assertEquals(((Map) actualResults.get(1).get("rewardPoints")).get("MARCH"), 7);
        Assert.assertEquals(((Map) actualResults.get(1).get("rewardPoints")).get("total"), 267);

    }
}
