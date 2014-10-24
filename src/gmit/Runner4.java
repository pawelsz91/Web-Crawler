package gmit;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator.ContainsOwnText;
/**
 * 
 * @author Pawel
 * 
 * <p>This class is a main runner class for my web crawler. It searches throught website using
 * mix of best first search, beam search and breadth search. It uses JSuop API to retrive website
 * contents and parse them as necessery to find best score.</p>
 * 
 */
public class Runner4
{
	
	private static List<UrlNode> visited_urls = new ArrayList<UrlNode>(); // Store all links already visited
	private static double keyword_count = 0, highest_score = 0, prev_highest_score = 0;
	private static final double div_val=0.03, html_val=0.14 , title_val = 20;
	private static Document doc;
    private static LinkedList<UrlNode> links = new LinkedList<UrlNode>(); //list of unvisited links
    private static LinkedList<UrlNode> children_links = new LinkedList<UrlNode>(); //list of unvisited links
    private static Elements div_words, html_words, title, headers;// select html and check does it have searched keywords
    private static String search_word, highest_score_url;
	private static HeuristicNodeComparator sorter = new HeuristicNodeComparator();
	private static UrlNode start = new UrlNode();
    
	public static void main(String[] args) throws IOException
	{
		String url_name = args[0];
		search_word = args[1];
		//String url_name = "http://www.gmit.ie/";
		//search_word = "gmit";
		url_name = url_name.toLowerCase();
		start = new UrlNode(url_name);
		start.setVisited(true);
		Runner4 r4 = new Runner4();
		r4.visit_url(start, search_word);
	}
	
	/**
	 * Main method that goes throu URL nodes, extracts their childern, scores them
	 * than picks the best one and goes all over again.
	 * 
	 * @param url
	 * 		Url address of a website made as an UrlNode object.
	 * @param search_word
	 * 		String containing a single word we will search for on websites
	 * @throws IOException
	 */
	public synchronized void visit_url(UrlNode url, String search_word) throws IOException
	{
		int cnt = 0;
		highest_score_url = url.toString();
		Runner4.search_word = search_word.toLowerCase();
	
		links.addFirst(url);
		while(!links.isEmpty())
		{			
			links.removeFirst();
			if(!visited_urls.toString().contains(url.toString()))
			{
				prev_highest_score = highest_score;
				visited_urls.add(url);
				url.setVisited(true);
				
				System.out.println();
				//visted URLs
				System.out.println("VISITED: "+ visited_urls.size());	
				//curently visited URL
				System.out.println("CURRENT: " + url.toString());
				
				get_document(url.toString());
				score_method(url.toString());
				url.addChildUrl(url, keyword_count);			

				try 
				{				
					extractLinks();
				} catch (Exception e) {
					e.printStackTrace();
				}
				//number of children URL found 
				System.out.println("LINKS TO SCORE: "+children_links.size());
				for (UrlNode link : children_links)
				{
				    get_document(link.toString());
				    score_method(link.toString());
				    url.addChildUrl(link, keyword_count);	    
				}
				
				//current url with highest score
				System.out.println(String.format("HIGHEST_SCORE: %.2f %s",highest_score, highest_score_url));

				//filtering through children_url and adding to links for later visiting
				UrlNode[] children_score_map = url.children_score_map();	
				for (int i = 0; i < children_score_map.length; i++) 
				{
					if (!children_score_map[i].isVisited())
					{
						if(!visited_urls.contains(children_score_map[i]))
						{
							if(!url.equals(children_score_map[i]))
							{
								children_score_map[i].setParent(url);
								links.add(children_score_map[i]);
							}
						}
					}
				}
				//number of links to visit
				System.out.println("LINKS TO VISIT: "+links.size());
				
				//making sure that links list does not contain current url
				if(links.contains(url))
				{
					links.remove(url);
				}
				
				//sorting links list to get URL with higher score in front of the list
				Collections.sort(links, sorter);
				url.setVisited(true);
				
				//checking if new highscore was found - if there is new highscore
				//reset counter 
				if(highest_score>prev_highest_score)
				{
					cnt = 0;
				}
			
				//if after visiting 150 urlNodes we dont get better highscore
				//find current highscore and set urlNode as a goal node
				if(cnt >= 200)
				{
					for (Entry<UrlNode, Double> entry : UrlNode.getScore_map().entrySet()) 
					{
			            if (entry.getValue().equals(highest_score)) 
			            {
			            	url = entry.getKey();
			            	url.setGoalNode(true);
			            }
			        }
					
				}
				
				if (url.isGoalNode())
				{
					System.out.println("This URL contains biggest number of relevant keywords: " + url.toString() +" " +highest_score);
					path(url);
					System.exit(0);
				}
				cnt++; //increase counter to se
			}
			
			
			try {
				//checking if links list doesnt contain url, then setting 1st element on list as 
				//next urlnode to visit
				if(!links.contains(url))
				{	
					url = links.getFirst();
					
					children_links.clear();
					UrlNode.getChildren_score_map().clear();	
				}
				//if links contains current url we remove it and take 1st element on list next
				else if(links.contains(url))
				{
					links.remove(url);
					url = links.getFirst();
					
					//clear children_links, and children_score_map for each iteration
					children_links.clear();
					UrlNode.getChildren_score_map().clear();
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
			}		
		}//end while()\
		System.out.println("LINKS EMPTY!");
	}		
	
	/**
	 * Method for scoring each website. Based on ammount of words and depending
	 * on how relevant the contents of a website are to the keyword the more points
	 * are given.
	 * 
	 * @param curr_page_url
	 * 		String containing url of a page that is currently being checked
	 * 
	 * @exception Catches any exceptions, mostly IO when sometimes div may be unaviable on website.
	 * 
	 */
	public static void score_method(String curr_page_url)
	{
		try 
		{
			keyword_count = 0;
			
			div_words = doc.select("div");
			html_words = doc.select("html");
			title = doc.select("title");
			//search for keywords in html, div and title
			count_keywords(search_word, html_words);
			count_keywords(search_word, div_words);
			count_keywords(search_word, title);
			
			//if url and title contains search word and if it starts with "http://www."+search_word
			//its probably the best match so boost the score by 270%
			if(curr_page_url.contains(search_word) && title.toString().toLowerCase().contains(search_word) 
					&& curr_page_url.startsWith("http://www."+search_word))
			{
				keyword_count = keyword_count*2.8;
			}
			//if any of this conditions are true boost score by 120%
			else if(curr_page_url.contains(search_word) || title.toString().toLowerCase().contains(search_word)
					|| curr_page_url.startsWith("http://www."+search_word))
			{
				keyword_count = keyword_count*1.2;
			}
			
			//if keyword count for this url is higher than highscore, set new highscore and url
			if(keyword_count> highest_score)
			{
				highest_score = keyword_count;
				highest_score_url = curr_page_url;
			}
		} 
		catch (Exception e) 
		{}
	}
	
	/**
	 * Connecting to desired website.
	 * @param url
	 * 		Url address of a website that program will connect to
	 * @throws IOException
	 */
	public static void get_document(String url){
		doc = null;		
		try {
			doc = Jsoup.connect(url).get();
		} 
		catch (IOException e1) 
		{}	
	}
		
	/**
	 * Method that will count and score importance of keywords on each website.
	 * Mainly for heuristics purposes.
	 * @param word
	 * 		String of a word we are looking for.
	 * @param page_text
	 * 		Element of a page we want to check for keywords. In this case its either
	 * 		title, div or html.
	 * @return Number of keywords found and extra values assigned to them
	 */
	public synchronized static double count_keywords(String word, Elements page_text)
	{
		String text = page_text.text().toLowerCase();
		int index = text.indexOf(word.toLowerCase());
		int wordLength = word.length();
		//scoring urlnodes with preseted values
		while (index != -1) {
		    if(page_text == title)
		    {
		    	keyword_count = keyword_count + title_val;
		    }
		    if(page_text == html_words)
		    {
		    	keyword_count = keyword_count + html_val;
		    }
		    if(page_text == div_words)
		    {
		    	keyword_count = keyword_count + div_val;
		    }
		    index = text.indexOf(word.toLowerCase(), index+wordLength);
		}		
		return keyword_count;
	}

	/**
	 * Extracting children nodes from curret node. This metod has some filttering as well,
	 * just to make sure we dont add links we already have or links we dont need to check.
	 * @return LinkedList<UrlNode> with children links.
	 */
	public LinkedList<UrlNode> extractLinks() 
	{
		try 
		{
			int cnt = 0;	
			Elements linkElements = doc.select("a[href]");
			for (Element link : linkElements) 
			{
				//if link is already visited or queued - skip to next
				if(!visited_urls.toString().contains(link.toString()) || !links.toString().contains(link.toString()))
				{
					//limiter for number of links, otherwise links list get out of control, and takes ages to look through
					//kind of bestfirst greedy approach mixed with beam search
					if(cnt>=35)
					{
						return children_links;
					}
					else
					{
						String href = link.attr("abs:href");
						if (href.indexOf("http") > -1) 
						{
							//some URL filters to not add links containing specific 
							if((href.contains("@") || href.contains(".pdf")
									//some websites like facebook/twitter/youtube are just never ending lists of links
									//which actually gives very fake score due to ammount of words in DIV and its hard to
									//properly score them, however just for sake of this example I let them be there...
									//|| href.contains("facebook") || href.contains("twitter") 
									//|| href.contains("youtube") 
									|| href.contains("?lang=")
									|| href.contains("#")) != true)
							{
								UrlNode newPage = new UrlNode(href.toLowerCase());
								if (!UrlNode.getScore_map().containsKey(newPage))
								{
									//dont add same links twice
									if(!children_links.toString().contains(newPage.toString()))
									{
										children_links.add(newPage);
									    cnt++;
									}
								}
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			
		}
		return children_links;
	}
	
	//display all parents of current node, making them path
	private void path(UrlNode url){
		List<UrlNode> path = new ArrayList<UrlNode>();
		while(url.getParent() != null){
			path.add(url);
			url = url.getParent();
		}
		path.add(url);
		Collections.reverse(path);
		System.out.println("Path: " + path);
	}
}

