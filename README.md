##red-dawn
========

open analytic platform

###NLP Storm stuff
========

-Models are needed, find them here:

http://opennlp.sourceforge.net/models-1.5/

-Topology, Spout, and initial bolt are specifically written for a handful of Orion Data Layer ASCII formatted files. Need to develop bolts to handle other formats

Annoying gotcha:

-The storm-contrib-mongo project references a parent pom that is not deployed into a repository. Here is how I fixed it (not ideal, but it worked)

1. Download the parent pom to a storm-contrib directory [here](https://raw.github.com/nathanmarz/storm-contrib/master/pom.xml "Parent POM")  
2. Comment out the modules  
3. mvn install it

