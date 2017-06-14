/*
Copyright 2015, SAP SE

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.sap.lumira.twitterextension;

import java.io.File;
import java.nio.file.Files;
import java.io.FileWriter;
import java.util.EnumSet;
import java.util.Set;
import java.util.List;
import java.util.Date;
import java.io.*;

import com.sap.bi.da.extension.sdk.DAEWorkflow;
import com.sap.bi.da.extension.sdk.DAException;
import com.sap.bi.da.extension.sdk.IDAEAcquisitionJobContext;
import com.sap.bi.da.extension.sdk.IDAEAcquisitionState;
import com.sap.bi.da.extension.sdk.IDAEClientRequestJob;
import com.sap.bi.da.extension.sdk.IDAEDataAcquisitionJob;
import com.sap.bi.da.extension.sdk.IDAEEnvironment;
import com.sap.bi.da.extension.sdk.IDAEMetadataAcquisitionJob;
import com.sap.bi.da.extension.sdk.IDAEProgress;
import com.sap.bi.da.extension.sdk.IDAExtension;

import org.json.JSONObject;
import twitter4j.*;
import twitter4j.conf.*;


public class TwitterExtension implements IDAExtension {

    public TwitterExtension() {
    }

    @Override
    public void initialize(IDAEEnvironment environment) {
    	// This function will be called when the extension is initially loaded
    	// This gives the extension to perform initialization steps, according to the provided environment
    }

    @Override
    public IDAEAcquisitionJobContext getDataAcquisitionJobContext (IDAEAcquisitionState acquisitionState) {
        return new TwitterExtensionAcquisitionJobContext(acquisitionState);
    }

    @Override
    public IDAEClientRequestJob getClientRequestJob(String request) {
        return new TwitterExtensionClientRequestJob(request);
    }

    private static class TwitterExtensionAcquisitionJobContext implements IDAEAcquisitionJobContext {

        private IDAEAcquisitionState acquisitionState;

        TwitterExtensionAcquisitionJobContext(IDAEAcquisitionState acquisitionState) {
            this.acquisitionState = acquisitionState;
        }

        @Override
        public IDAEMetadataAcquisitionJob getMetadataAcquisitionJob() {
            return new TwitterExtensionMetadataRequestJob(acquisitionState);
        }

        @Override
        public IDAEDataAcquisitionJob getDataAcquisitionJob() {
            return new TwitterExtensionDataRequestJob(acquisitionState);
        }

        @Override
        public void cleanup() {
        	// Called once acquisition is complete
        	// Provides the job the opportunity to perform cleanup, if needed
        	// Will be called after both job.cleanup()'s are called
        }
    }

    private static class TwitterExtensionDataRequestJob implements IDAEDataAcquisitionJob
    {
        IDAEAcquisitionState acquisitionState;

        TwitterExtensionDataRequestJob (IDAEAcquisitionState acquisitionState) {
            this.acquisitionState = acquisitionState;
        }

        @Override
        public File execute(IDAEProgress callback) throws DAException {
            try {
                IDAEAcquisitionState as = acquisitionState;
                
                //Get info object from acquisitionState
            	JSONObject infoJSON = new JSONObject(acquisitionState.getInfo());
                
                //Get OAuth keys and tokens from the info object to authenticate the user
            	//(using twitter4j library)
                ConfigurationBuilder cb = new ConfigurationBuilder(); 
                cb.setDebugEnabled(true)
	            	.setOAuthConsumerKey(infoJSON.getString("consumerKey"))
	            	.setOAuthConsumerSecret(infoJSON.getString("consumerSecret"))
	            	.setOAuthAccessToken(infoJSON.getString("accessToken"))
	            	.setOAuthAccessTokenSecret(infoJSON.getString("accessSecret"));
	             
                //Use the twitter4j library to fetch the authenticated user's timeline
                TwitterFactory tf = new TwitterFactory(cb.build());
                Twitter twitter = tf.getInstance();
                Paging page = new Paging (1, 100);
                List<Status> status = twitter.getUserTimeline(page);
                
                //Prepare to create a CSV file: store the comma delimeter, new line character, and column header
                String delimeter = ",";
                String newLine = "\n";
                String header = "Tweet,Created_At,Favorites,Retweets";
                
                
                FileWriter fileWriter = null;
                
                try {
                	//Provide a location for the FileWriter to create the CSV file
                	fileWriter = new FileWriter("C:\\Users\\i859536\\workspace\\com.sap.lumira.twitterextension\\src\\com\\sap\\lumira\\twitterextension\\tweets.csv");
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
                File csv = new File("C:\\Users\\i859536\\workspace\\com.sap.lumira.twitterextension\\src\\com\\sap\\lumira\\twitterextension\\tweets.csv");
                return csv;
            } catch (Exception e) {
                throw new DAException("Twitter Extension acquisition failed", e);
            }
        }

        @Override
        public void cancel() {
        	// Cancel is currently not supported
        }

        @Override
        public void cleanup() {
        	// Called once acquisition is complete
        }
    }

    private static class TwitterExtensionMetadataRequestJob implements IDAEMetadataAcquisitionJob {
        IDAEAcquisitionState acquisitionState;

        TwitterExtensionMetadataRequestJob (IDAEAcquisitionState acquisitionState) {
            this.acquisitionState = acquisitionState;
        }

        @Override
        public String execute(IDAEProgress callback) throws DAException {
            try {
            	//Retrieve your metadata file from it's location, and read it to a String
                File metadataFile = new File("C:\\Users\\i859536\\workspace\\com.sap.lumira.twitterextension\\src\\com\\sap\\lumira\\twitterextension\\metadata.txt");
                String metadata = new String(Files.readAllBytes(metadataFile.toPath()));
                return metadata;
            } catch (Exception e) {
                throw new DAException("Twitter Extension acquisition failed", e);
            }
        }

        @Override
        public void cancel() {
        	// Cancel is currently not supported
        }

        @Override
        public void cleanup() {
        	// Called once acquisition is complete
        }
    }

    private class TwitterExtensionClientRequestJob implements IDAEClientRequestJob {

        String request;

        TwitterExtensionClientRequestJob(String request) {
            this.request = request;
        }

        @Override
        public String execute(IDAEProgress callback) throws DAException {
            return null;
        }

        @Override
        public void cancel() {
        	// Cancel is currently not supported
        }

        @Override
        public void cleanup() {
        	// This function is NOT called
        }

    }

    @Override
    public Set<DAEWorkflow> getEnabledWorkflows(IDAEAcquisitionState acquisitionState) {
    	// If the extension is incompatible with the current environment, it may disable itself using this function
    	// return EnumSet.allOf(DAEWorkflow.class) to enable the extension
    	// return EnumSet.noneOf(DAEWorkflow.class) to disable the extension
    	// Partial enabling is not currently supported
        return EnumSet.allOf(DAEWorkflow.class);
    }
}
