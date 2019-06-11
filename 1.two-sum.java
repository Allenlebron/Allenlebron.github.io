import java.util.HashMap;
import java.util.Map;

/*
 * @lc app=leetcode id=1 lang=java
 *
 * [1] Two Sum
 */
class Solution {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            map.put(nums[i], i);
        }
        for (int i = 0; i < nums.length; i++) {
            int index = target - nums[i];
            if (map.containsKey(index) && map.get(index) != i) {
                return new int[] { i, map.get(index) };
            }
        }
        throw new IllegalArgumentException("No two sum solution");
    }
}

