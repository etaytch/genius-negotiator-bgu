import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import misc.Pair;


public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
			System.out.println(cartesianProduct(Arrays.asList(Arrays.asList("Apple", "Banana"), Arrays.asList("Red", "Green", "Blue"))));
	}

	
	
	public static List<List<String>> cartesianProduct(List<List<String>> lists) {
		  Pair<String, String> d;
		  List<List<String>> resultLists = new ArrayList<List<String>>();
		  if (lists.size() == 0) {
		    resultLists.add(new ArrayList<String>());
		    return resultLists;
		  } else {
		    List<String> firstList = lists.get(0);
		    List<List<String>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
		    for (String condition : firstList) {
		      for (List<String> remainingList : remainingLists) {
		        ArrayList<String> resultList = new ArrayList<String>();
		        resultList.add(condition);
		        resultList.addAll(remainingList);
		        resultLists.add(resultList);
		      }
		    }
		  }
		  return resultLists;
	}
	
}
