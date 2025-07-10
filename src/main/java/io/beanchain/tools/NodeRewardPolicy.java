package io.beanchain.tools;

public class NodeRewardPolicy {

    private static final double INITIAL_REWARD = 0.01;         // in BEAN
    private static final long HALVING_INTERVAL = 500_000;      // blocks
    private static final double MIN_REWARD = 0.000001;         // lowest drip allowed (1 BEANTOSHI)

    /**
     * Returns the node reward in BEAN for a given block height.
     *
     * @param blockHeight current chain height
     * @return reward in BEAN (e.g. 0.01, 0.005, etc.)
     */
    public static double getNodeReward(long blockHeight) {
        int halvings = (int) (blockHeight / HALVING_INTERVAL);
        double reward = INITIAL_REWARD / Math.pow(2, halvings);
        return Math.max(reward, MIN_REWARD);
    }
}