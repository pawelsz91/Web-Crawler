package gmit;

import java.util.*;
public class HeuristicNodeComparator implements Comparator<UrlNode>{
	public int compare(UrlNode url1, UrlNode url2) {
		if (url1.getScore_map().get(url1) < url2.getScore_map().get(url2)){
			return 1;
		}else if (url1.getScore_map().get(url1) > url2.getScore_map().get(url2)){
			return -1;
		}else{
			return 0;
		}
	}
}
