public abstract class PathNodeUtil {
	public static final int RADIX = 36;
	public static final int LEN = 3;
	public static final char PATH_SPLITER_CHAR = '/';
	public static final String ROOT_PATH = "";
	public static final String PATH_SPLITER = ""+PATH_SPLITER_CHAR;
	public static final String MATCHER = repeat("_",LEN);
	public static final int MAX = power(RADIX,LEN);
	public static final String TEMPLATE = repeat("0",LEN);
	
	public static String repeat(String s,int times){
		if(s==null || s.equals("")){
			return s;
		}
		String res = "";
		for(int i=0;i<times;i++){
			res+=s;
		}
		return res;
	}
	public static int power(int a,int x){
		double res = Math.pow(a, x);
		if(res<Integer.MAX_VALUE){
			return (int)res;
		}
		throw new RuntimeException("power result out of range!");
	}
	public static void main(String[] args) {
		System.out.println(power(10,2));
	}
	
}
