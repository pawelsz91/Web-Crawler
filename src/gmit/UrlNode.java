package gmit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 
 * @author Pawel
 *
 *
 * <p> This class helps to set some basic method we want our UrlNode object to have 
 * when we are scrapping website and scoring each node and then putting it into HashMap</p>
 */
public class UrlNode {

	private UrlNode parent;
	private static Map<UrlNode, Double> children_score_map = new ConcurrentHashMap<UrlNode, Double>();
	private static Map<UrlNode, Double> score_map = new ConcurrentHashMap<UrlNode, Double>();
	private boolean visited = false;
	private boolean goalNode;
	private String url_name;

	/**
	 * 
	 * @return KeySet of children_score_map
	 */
	public UrlNode[] children_score_map(){
		return (UrlNode[]) getChildren_score_map().keySet().toArray(new UrlNode[getChildren_score_map().size()]);
	}
	
	/**
	 * 
	 * @return KeySet of score_map
	 */	
	public UrlNode[] score_map(){
		return (UrlNode[]) getScore_map().keySet().toArray(new UrlNode[getScore_map().size()]);
	}
	
	/**
	 * Makes sure that url is correct
	 * @param urlArg
	 * 		String containing url address
	 */
	public UrlNode(String urlArg) 
	{
		int n = urlArg.length();
		if (urlArg.charAt(n-1) != '/')
			this.url_name = urlArg + "/";
		else
			this.url_name = urlArg;
	}
	
	public UrlNode() {}
	
	public String toString() {
		return this.url_name;
	}
    
	public UrlNode getParent() {
		return parent;
	}

	public void setParent(UrlNode parent) {
		this.parent = parent;
	}
	
	public boolean isGoalNode() {
		return goalNode;
	}

	public void setGoalNode(boolean goalNode) {
		this.goalNode = goalNode;
	}
    
	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	public int getChildUrlCount(){
		return getScore_map().size();
	}
	
	/**
	 * Adding child url node to children_score_map and score_map
	 * @param child
	 * 		UrlNode child node to be added to hash maps
	 * @param keywordCount
	 * 		Score of a url node represented as Double
	 */
	public void addChildUrl(UrlNode child, double keywordCount){
		getScore_map().put(child, keywordCount);
		getChildren_score_map().put(child, keywordCount);
	}
	
	public void removeChild(UrlNode child){
		getScore_map().remove(child);
	}
	
	public String getUrlName() {
		return url_name;
	}

	public void setUrlName(String urlName) {
		this.url_name = urlName;
	}

	public static Map<UrlNode, Double> getScore_map() {
		return score_map;
	}

	public void setScore_map(Map<UrlNode, Double> score_map) {
		UrlNode.score_map = score_map;
	}

	public static Map<UrlNode, Double> getChildren_score_map() {
		return children_score_map;
	}

	public static void setChildren_score_map(Map<UrlNode, Double> children_score_map) {
		UrlNode.children_score_map = children_score_map;
	}
	
}
