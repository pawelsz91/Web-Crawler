1.Instructions
In terminal go to path where fuzzy.jar is located and type in command:
java -jar fuzzy.jar http://www.google.com ireland
Crawler will start from google.com and it will search for keyword ireland.
I'm sending you tested version witch compiles without any problems and run 
without any errors. However, if you come across any difficulties (cross-platform)
let me know.

2.Architecture
This crawler is based on mix of best first greedy search and beam search.
It will visit the website, grab 35 links (filtering them before) add them to
children_url list. Then it will go and visit every single children_url, score them
and add them to HashMap and links list. Then based on the score links list is sorted
and first element which will be curently highest score on links list will be taken 
as current URL and explored as well as removed from links list and added to visited list.
If after 200 visits there is no new highscore, check is performed for current highscore 
and url with highscore is seted as a goal node. Goal node is then displayed and all 
parent url nodes are showed as path. Crawler takes in only one keyword.
I decided create scoring system without JFuzzy API. I created 2 methods to help me 
score websites based on the number of keywords which I then use for scoring. Score
is adjusted based on certain rules like if title contains keyword or if url address 
contains keyword etc. I did run good few test to adjust them best to my ability, however
because we deal with keywords some results are very fake. For example www.gmit.ie has only 
around 100xgmit keyword occuring in comparison to www.youtube.com/gmitchannel which has nearly
1700xgmit keyword occuring... So it was rather hard to score them properly but after runing
some tests I did the best I could think of to score them correctly.

3.Known drawbacks
I did not implement multithreading so it will slow down the crawler. Also this 
Crawler due to its architecture is mainly dependet on internet speed, because
of scoring system when I decided to firstly visit each children_url and score it.

4.Libs and Docs
Extra libraries required to run the crawler have been added inside jar file, as well
as in separate folder. Extra documents describing methods have been created. 
Most of the code is commented, and the main logic should be easy to understand.

5.Author
Pawel Szulc, G00280690