public class Credits {
    private static double ALPHA = 1.5;
    private static long THRESHOLD = 3600;

    public static long getAfterAdding(long currentCredits, long timeOnGridInMilliSecs){
        return (long) (currentCredits + Math.floor(timeOnGridInMilliSecs/1000));
    }

    public static long getAfterBurning(long currentCredits, long timeOfComputeInMilliSecs){
        return (long) (currentCredits - ALPHA*Math.floor(timeOfComputeInMilliSecs/1000));
    }

    public static boolean canCompute(long currentCredits){
        if(currentCredits > THRESHOLD){
            return true;
        } else{
            return false;
        }
    }
}
