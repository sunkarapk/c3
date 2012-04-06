public class Credits {
    private static double ALPHA = 1.5;
    private static long THRESHOLD = 3600;

    public static long getAfterAdding(long currentCredits, long timeOnGridInSecs){
        return currentCredits + timeOnGridInSecs;
    }

    public static long getAfterBurning(long currentCredits, long timeOfComputeInSecs){
        return (long) (currentCredits - ALPHA*timeOfComputeInSecs);
    }

    public static boolean canCompute(long currentCredits){
        if(currentCredits > THRESHOLD){
            return true;
        } else{
            return false;
        }
    }
}
