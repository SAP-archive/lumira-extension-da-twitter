## Building Data Access Extensions

### Requirements
<ul>
	<li>SAP Lumira Discovery or SAP Lumira 1.29+</li>
	<li>Java Development Kit 7, Update 75+</li>
	<li>Eclipse IDE for Java EE Developers</li>
</ul>

Here we will demonstrate how to build your own Data Access Extension, by using the sample extension as starter code. In this example we will also show how to utilize external libraries, store parameters temporarily in the "runtimeInfo" object, and add custom icons to build a lightweight extension for Twitter data. 

We begin by downloading and unzipping the sample extension folder. Then navigate to the folder, copy-paste it, and rename it to our extension's name.

![](/photos/21-renamed-folder.PNG)

Next, we also have to rename various files to match our new extension's name. Luckily, the sample extension comes equipped with a tool that does this for you easily. 

Open the folder, navigate into the "Docs" sub-folder and run rename-dae.exe. The rename tool will open a prompt:

![](/photos/22-prompt.PNG)

Enter a name for your extension with no spaces (e.g. "Twitter"), and the package path "com.sap.bi.da.extension". 

![](/photos/23-prompt-filled.PNG)

Press enter, and the tool will handle the renaming task for you. Now, we're ready to open our extension project in Eclipse.

###### Import Project

In the menu bar of our Eclipse workspace, we navigate to File > Import. Then, click "Existing Projects into Workspace".

![](/photos/24-import.PNG)

Browse to the new extension folder, and select the "Copy projects into workspace" checkbox (optional, but recommended). 

![](/photos/25-import-finish.PNG)

Lastly, click finish to import our extension project into Eclipse

###### Environment Setup

Right-click the Docs > eclipse.bat file, and select "Open With > Text Editor". Set the "ECLIPSE HOME" environment variable to your Eclipse installation folder path, and "JAVA HOME" to your JDK folder path.

![](/photos/26-eclipse-bat.PNG)

Next up, we open the file "platform.target" and click the "Set as Target Platform" button in the top-right corner.

![](/photos/27-target-platform.PNG)

Note: ignore any errors in plugin.xml

Then, we right click the "export.xml" file and "Run As > Ant Build" to export our extension zip file. 

Note: If your build is not successful due to an error with the "zip64mode" attribute, remove the two instances of "zip64mode=never" in the export.xml file and rebuild.

You can navigate to the target folder in your File Explorer to find the extension zip file, that can be installed in Lumira using the Extension Manager. 

Note: if you selected the "Copy Projects into workspace" checkbox when importing the project into Eclipse, the target folder will be located in the copy located inside your workspace directory (e.g. "C:\Users\YOUR_USERNAME\workspace")

Right now the extension may have a different name, but it still has the same functionality as the sample. Let's start modifying it for our use case. 

###### Twitter Platform

First, we register an application on the Twitter Platform. Sign up for a developer account at [http://dev.twitter.com](http://dev.twitter.com).

After logging in, click "My Apps" in the navigation menu at the top of the platform site. 

![](/photos/28-twitter-platform.PNG)

Next, click the "Create New App" button

![](/photos/29-create-app.PNG)

We fill out the form with our app's name, description, and URL (can just be a placeholder), check the Developer Agreement, and click "Create Your Twitter Application".

![](/photos/30-app-details-form.PNG)

In the application page, navigate to "Keys and Access Tokens". Take note of your Consumer Key and Consumer Secret. Then, scroll down to the bottom of the page to generate your Access Token and Access Secret. These keys and tokens will be needed for authentication when we use our extension. 

###### TwitterExtensionDialogController.js

On the frontend, we need to change our dialog controls to match the parameters we will need from the user. So, we replace the sample UI with text inputs for the user's Consumer Key, Consumer Secret, Access Token, and Access Secret. 

```javascript
var datasetNameLabel = new sap.m.Label({
        	text: "Dataset Name:",
        	labelFor: datasetNameText
        });
        
        dialog.addContent(datasetNameLabel);
        
        var datasetNameText = new sap.m.Input({
        	width: "100%"
        });
        
        dialog.addContent(datasetNameText);
        
        var consumerKeyLabel = new sap.m.Label({
            text : "Consumer Key:",
            labelFor : consumerKeyText
        });
        
        dialog.addContent(consumerKeyLabel);

        var consumerKeyText = new sap.m.Input({
            width : '100%'
        });
        
        dialog.addContent(consumerKeyText);
        
        var consumerSecretLabel = new sap.m.Label({
            text : "Consumer Secret:",
            labelFor : consumerSecretText
        });
        
        dialog.addContent(consumerSecretLabel);

        var consumerSecretText = new sap.m.Input({
            width : '100%'
        });
        
        dialog.addContent(consumerSecretText);
        
        var accessTokenLabel = new sap.m.Label({
            text : "Access Token:",
            labelFor : accessTokenText
        });
        
        dialog.addContent(accessTokenLabel);
        
        var accessTokenText = new sap.m.Input({
            width : '100%'
        });
        
        dialog.addContent(accessTokenText);
        
        var accessSecretLabel = new sap.m.Label({
            text : "Access Secret:",
            labelFor : accessSecretText
        });
        
        dialog.addContent(accessSecretLabel);
        
        var accessSecretText = new sap.m.Input({
            width : '100%'
        });
              
        dialog.addContent(accessSecretText);
```

Next, we use the "runtimeInfo" object to store those objects rather than just the "info" object in acquisitionState. This way, our user's sensitive keys and tokens will not persist with the Lumira document. 

```javascript
var okButtonPressed = function() {
            var info = {};
            var runtimeInfo = {};
            info.datasetName = datasetNameText.getValue();
            runtimeInfo.consumerKey = consumerKeyText.getValue();
            runtimeInfo.consumerSecret = consumerSecretText.getValue();
            runtimeInfo.accessToken =  accessTokenText.getValue();
            runtimeInfo.accessSecret = accessSecretText.getValue();
            acquisitionState.info = JSON.stringify(info);
            acquisitionState.runtimeInfo = JSON.stringify(runtimeInfo);
            oDeferred.resolve(acquisitionState, info.datasetName);
            dialog.close();
        };
```

###### Adding External Libraries

In our backend, we'll be using an external java library "twitter4j" to interact with the Twitter API. 

To add external libraries to our extension library, we first paste the .jar file inside of our "/lib" folder. 

![](/photos/31-lib-jar.PNG)

Next, right-click the .jar file in Eclipse and select "Build Path > Add to Build Path".

Lastly navigate to the "META-INF" folder in Eclipse, right-click the "MANIFEST.MF" file and select "Open With > Text Editor" to edit. Here, we add the path of our external library (e.g. "lib/twitter4j-core-4.0.4.jar") to the "Bundle-Classpath" in our manifest. 

![](/photos/32-manifest.PNG)

###### TwitterExtension.java

Now we are ready to use the library in our Java code. 

We start with the execute() method in our TwitterExtensionDataRequestJob class. Here, we retrieve the parameters the user entered in the UI from the acquisitionState runtime info. 

```java
//Get runtime info object from acquisitionState
            	JSONObject runtimeInfoJSON = new JSONObject(acquisitionState.getRuntimeInfo());
```


Next, we use the twitter4j library to authenticate the user based on those parameters and fetch their tweets

```java
//Get OAuth keys and tokens from the info object to authenticate the user
            	//(using twitter4j library)
                ConfigurationBuilder cb = new ConfigurationBuilder(); 
                cb.setDebugEnabled(true)
	            	.setOAuthConsumerKey(runtimeInfoJSON.getString("consumerKey"))
	            	.setOAuthConsumerSecret(runtimeInfoJSON.getString("consumerSecret"))
	            	.setOAuthAccessToken(runtimeInfoJSON.getString("accessToken"))
	            	.setOAuthAccessTokenSecret(runtimeInfoJSON.getString("accessSecret"));
	             
                //Use the twitter4j library to fetch the authenticated user's timeline
                TwitterFactory tf = new TwitterFactory(cb.build());
                Twitter twitter = tf.getInstance();
                Paging page = new Paging (1, 100);
                List<Status> status = twitter.getUserTimeline(page);
```

Then we loop through the user's timeline and write each tweet, creation date, number of likes, and number of retweets to a CSV file using the FileWriter class. 

```java
//Prepare to create a CSV file: store the comma delimeter, new line character, and column header
                String delimeter = ",";
                String newLine = "\n";
                String header = "Tweet,Created_At,Favorites,Retweets";
                
                FileWriter fileWriter = null;
                
                try {
                	//Provide a location for the FileWriter to create the CSV file
                	fileWriter = new FileWriter("YOUR_FILE_PATH");
                	fileWriter.append(header);
                	
                	String tweet;
                	Date createdAt; 
                	int numFavorites;
                	int numRetweets;
                	
                	//Loop through the tweets in the user's timeline
                	//and append the tweet, time it was created, favorites, and retweets to the CSV
                	for (Status st: status) {
                		if (!st.isRetweet()) {
	                		fileWriter.append(newLine);
	                    	tweet = st.getText();
	                    	createdAt = st.getCreatedAt();
	                    	numFavorites = st.getFavoriteCount();
	                    	numRetweets = st.getRetweetCount();
	                    	fileWriter.append(tweet);
	                    	fileWriter.append(delimeter);
	                    	fileWriter.append(String.valueOf(numFavorites));
	                    	fileWriter.append(delimeter);
	                    	fileWriter.append(String.valueOf(numRetweets));
                		}
                    }
                	
                } catch (Exception e) {
                	throw new DAException("Filewriter failed", e);
                } finally {
                	try {
                		fileWriter.flush();
                		fileWriter.close();
                	} catch (Exception e) {
                		throw new DAException("Error flushing/closing filewriter", e);
                	}
                }
                
                //Return the newly created CSV as a File object
                File csv = new File(""YOUR_FILE_PATH"");
                return csv;
            } catch (Exception e) {
                throw new DAException("Twitter Extension acquisition failed", e);
            }
```

Lastly, we return the CSV we wrote as a File object.

We move to the execute() method in the TwitterExtensionMetadataRequestJob class.

Before we modify the code here, we'll need to create a metadata file for our data and paste it in our project. 

![](/photos/34-metadata-tweet.PNG)

![](/photos/35-metadata-file-structure.PNG)

Now, we can instantiate our metadata as a File object and read it as a String that we return.

```java
 @Override
        public String execute(IDAEProgress callback) throws DAException {
            try {
            	//Retrieve your metadata file from it's location, and read it to a String
                File metadataFile = new File("YOUR_FILE_PATH");
                String metadata = new String(Files.readAllBytes(metadataFile.toPath()));
                return metadata;
            } catch (Exception e) {
                throw new DAException("Twitter Extension acquisition failed", e);
            }
        }
```

###### TwitterExtension.js & Custom icons

In the TwitterExtension.js file, we can first change the title and subtitle of our dataset here.

```javascript
// This function must return an Object with properties Title and SubTitle, determined by the provided acquisitionState
    // This will be displayed as an entry in the Most Recently Used pane
    TwitterExtension.prototype.getConnectionDescription = function(acquisitionState) {
        var info = JSON.parse(acquisitionState.info);
        return {
            Title: info.datasetName,
            SubTitle: "Twitter Dataset"
        };
    };
```

A more exciting change here is the ability to add custom icons. 
You can use this website to easily generate different sizes for your icon. 

Navigate to the "img" folder within the "WebContent" folder. Here you can paste your icon image files. You can either use the same file names as the sample and replace, or alternatively you will have to change the file names in the TwitterExtension.js file

![](/photos/36-img-folder.PNG)

```javascript
// getIcon## must return a path to an image with size ##px* ##px
    TwitterExtension.prototype.getIcon48 = function() {
        return "/img/48.png";
    };
    TwitterExtension.prototype.getIcon32 = function() {
        return "/img/32.png";
    };
    // The white version of the icon will be displayed when the extension is highlighted in the New Dataset dialog
    TwitterExtension.prototype.getIcon32_white = function() {
        return "/img/32_w.png";
    };
    TwitterExtension.prototype.getIcon24 = function() {
        return "/img/24.png";
    };
    TwitterExtension.prototype.getIcon16 = function() {
        return "/img/16.png";
    };
```

Our extension is now complete and ready to use!

![](/photos/37-extension-dialog.PNG)

![](/photos/38-tweet-data.PNG)

### [Back to Overview](/README.md)
### [Sample Extension](/sample-da.md)
### [Debugging](/debugging.md)













































