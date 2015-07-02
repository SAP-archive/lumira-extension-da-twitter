lumira-extension-da-twitter
===========================

A Lumira Data Access Extension To Fetch Twitter Data

A Step-by-Step Guide to Installation & Execution of Twitter Data Access Extension is shown as follows:

<strong>Step 1:	Creation of a Twitter app</strong> <br>
The reason why we need to create a Twitter app is so that we can access Twitter API keys, which will be our main source of      extracting Twitter data and importing it into SAP Lumira for visualization. <br>
1. Simply log in to your Twitter account (or sign up if you don’t already have one), and then go to   https://apps.twitter.com/ and create a new app.
![My image](https://github.com/SAP/lumira-extension-da-twitter/blob/master/readmescreenshots/1.jpg)<br>
![My image](https://github.com/SAP/lumira-extension-da-twitter/blob/master/readmescreenshots/2.jpg) <br>
2. You now have your Twitter app from which you can grab your API keys which we will use later. 
![My image](https://github.com/SAP/lumira-extension-da-twitter/blob/master/readmescreenshots/3.jpg) 
<br>

<strong>Step 2:	Activate Data Source Extensions in Lumira</strong> <br>
1. Go to the directory where SAP Lumira is installed: C:\Program Files\SAP Lumira\Desktop <br>
2. Find the file SAPLumira.ini and open it with a text editor. <br>
![My image](https://github.com/SAP/lumira-extension-da-twitter/blob/master/readmescreenshots/a.jpg) <br>
3. Add the following lines of code to the SAPLumira.ini file: <br>
  -Dhilo.externalds.folder=C:\Program Files\SAP Lumira\Desktop\daextensions <br>
  -Dactivate.externaldatasource.ds=true <br>
![My image](https://github.com/SAP/lumira-extension-da-twitter/blob/master/readmescreenshots/b.jpg)<br>
4. Now save this file and create a folder called daextensions in the C:\Program Files\SAP Lumira\Desktop directory so that we have a directory called C:\Program Files\SAP Lumira\Desktop\daextensions <br>
5. Move the executable file called TwitterExtractor.exe located in the \bin folder, to the directory we just created.<br>

<strong>Step 3:	Import data extension into Lumira</strong> <br>
1. Now open up Lumira and add a new dataset from an external data source as follows:<br>
![My image](https://github.com/SAP/lumira-extension-da-twitter/blob/master/readmescreenshots/d.jpg)<br>
2. We can see the twitterextractor as an uncategorized extension:<br>
![My image](https://github.com/SAP/lumira-extension-da-twitter/blob/master/readmescreenshots/e.jpg)<br>

<strong>Step 4:	Provide extraction parameters</strong> <br>
The TwitterExtractor.exe will automatically open up when we click on Next above. Enter the parameters like the API Key and API Secret, the string we want to search for, and the number of tweets we want to display in the data chart. <br>
![My image](https://github.com/SAP/lumira-extension-da-twitter/blob/master/readmescreenshots/c.jpg)<br>

<strong>Step 5:	Create dataset</strong> <br>
Live Twitter data will be imported as a dataset, which we can choose to create.<br>
![My image](https://github.com/SAP/lumira-extension-da-twitter/blob/master/readmescreenshots/f.jpg)<br>

<strong>Step 6:	Get live insights</strong> <br>
Once you have the dataset imported into Lumira, you can now play around with the data and charts as you please!<br>
![My image](https://github.com/SAP/lumira-extension-da-twitter/blob/master/readmescreenshots/g.jpg)<br>


You can find details on this SCN blog post : <br>
<a>http://scn.sap.com/community/lumira/blog/2014/09/12/a-lumira-extension-to-acquire-twitter-data</a>

NOTE: <br>
Due to Twitter's REST API policy on limited data retrieval, only a small amount of data can be called, with a limited time frame of about a week (for open source, free users) and 30 days for paid users. Data charts generated in Lumira may therefore have limited and/or less data points. 
