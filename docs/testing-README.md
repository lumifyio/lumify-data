ALL INCLUSIVE TESTING DATASET
=============================

Contents
--------

1. 9a1467ce396eb62053260105c8459a3d13cf11bf618098e4f0b652bfa58d7777:
	* DESCRIPTION: A web-crawled article about Edward Snowden
	* EXPECTED OUTCOME: A document without a file extension should have its information displayed
2. badFile:
	* DESCRIPTION: A blank file without an extension
	* EXPECTED OUTCOME: The document should be displayed because it has no bad extension
3. bffls.txt:
	* DESCRIPTION: A text document containing information about Joe and Sam at Near Infinity
	* EXPECTED OUTCOME: The document should be displayed and have a phone number, email address, name, organization, location, and zip code
4. blank.txt:
	* DESCRIPTION: A text document without content
	* EXPECTED OUTCOME: The blank file should be displayed as is
5. crime2.pdf:
	* DESCRIPTION: A pdf document of the factors that affect crime rates in the country
	* EXPECTED OUTCOME: The document should be displayed as is
6. good.foo:
	* DESCRIPTION: A good file with a bad extension
	* EXPECTED OUTCOME: The file should be displayed as a normal document
7. headline-la-times-boston-marathon-bombing-500x438.jpg:
	* DESCRIPTION: A picture of a news paper about the Boston bombing
	* EXPECTED OUTCOME: The picture should have "TERROR IN BOSTON" and a face extracted from it (the location of the face is not an actual face)
8. i-heart-chicago-police.png:
	* DESCRIPTION: A picture of a couple holding a sign for the CPD
	* EXPECTED OUTCOME: The image should be displayed as normal for images
9. map.csv:
	* DESCRIPTION: A csv of office locations and their employee head-count
	* EXPECTED OUTCOME: The document should be displayed as plain text because there is no mapping
10. near_infinity.html:
	* DESCRIPTION: An html document about the beginning of Near Infinity
	* EXPECTED OUTCOME: The text part of the document should be displayed without any strange encodings
11. neighborhood.json:
	* DESCRIPTION: A json document with information about crime reports in Chicago
	* EXPECTED OUTCOME: The contents of the file should be displayed as raw text
12. organizational-chart.gif:
	* DESCRIPTION: An image of the organizational flow of the officers in the CPD
	* EXPECTED OUTCOME: The image should be displayed as normal
13. personLocations.lumify.tar:
	* DESCRIPTION: A csv and mapping of people, where they live and their birthday's
	* EXPECTED OUTCOME: Entities for each of the people should be created with the appropriate properties and relationships
14. test-video-with-transcript.lumify.tar:
	* DESCRIPTION: A video and srt file about British nationals who were attacked
	* EXPECTED OUTCOME: The video should play as normal with the transcript scrolling of both the srt and ocr-ed text
15. tiny.flv:
	* DESCRIPTION: A video about Obama going to Chicago
	* EXPECTED OUTCOME: The video should play as normal with only ocr-ed text
16. ytcc.lumify.tar:
	* DESCRIPTION: A video and youtubecc file of a woman giving an explination of a fire
	* EXPECTED OUTCOME: The video should play as normal with the scrolling transcript from the file and any ocr-ed text
	
[Everything](https://s3.amazonaws.com/RedDawn/DataSets/testing.zip)

[The dataset](https://s3.amazonaws.com/RedDawn/DataSets/testing-data.zip)

[The dictionaries](https://s3.amazonaws.com/RedDawn/DataSets/testing-dictionaries.zip)

[The ontology](https://s3.amazonaws.com/RedDawn/DataSets/testing-ontology.zip)


