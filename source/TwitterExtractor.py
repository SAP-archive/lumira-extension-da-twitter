# -*- coding: utf-8 -*-
import logging
#imports
import httplib
import socket
from tweepy import API
#from tweepy import Stream
from tweepy import OAuthHandler
from tweepy import Cursor
from tweepy import TweepError
#from tweepy.streaming import StreamListener
import easygui
import sys
reload(sys)
sys.setdefaultencoding("utf-8")



def enum(*sequential, **named):
    enums = dict(zip(sequential, range(len(sequential))), **named)
    return type('Enum', (), enums)

Mode = enum('PREVIEW', 'EDIT', 'REFRESH')
mode = 0


paramslist = []
key = ''
i = 0
query = ''
items=''
msg = "Enter Required Information"
title = "Tweet Extractor"
fieldNames = ["API Key","API Secret",
              #"Access Token Key","Access Token Secret",
              "Search String", "Max. Tweets"]

fieldValues = []  # we start with blanks for the values
for i in range(4):
    fieldValues.append(i)

for i in range(len(sys.argv)):
    if str(sys.argv[i]).lower() == "-mode" and (i + 1) < len(sys.argv):
        if str(sys.argv[i + 1]).lower() == "preview":
            mode = Mode.PREVIEW
        elif str(sys.argv[i + 1]).lower() == "edit":
            mode = Mode.EDIT
        elif str(sys.argv[i + 1]).lower() == "refresh":
            mode = Mode.REFRESH
    elif str(sys.argv[i]).lower() == "-size":
        size = int(sys.argv[i + 1])
    elif str(sys.argv[i]).lower() == "-params":
        params = str(sys.argv[i + 1])
        paramslist = params.split(';')

    i += 1



def setArgs(fieldValues):

    fieldValues[0] = ''
    fieldValues[1] = ''
    fieldValues[2] = ''
    fieldValues[3] = ''
    return fieldValues



def parseArgs(fieldValues):

    #if paramslist is None: break

    for i in range(len(paramslist)):
        if paramslist[i].split('=')[0].lower() == 'api_key':
            try:
                fieldValues[0] = paramslist[i].split('=')[1].decode('hex')
            except:
                fieldValues[0] = 'ENTER_API_KEY'
        elif paramslist[i].split('=')[0].lower() == 'api_secret':
            try:
                fieldValues[1] = paramslist[i].split('=')[1].decode('hex')
            except:
                fieldValues[1] = 'ENTER_API_SECRET'
        #elif paramslist[i].split('=')[0].lower() == 'access_token':
            #fieldValues[2] = paramslist[i].split('=')[1]
        #elif paramslist[i].split('=')[0].lower() == 'access_secret':
            #fieldValues[3] = paramslist[i].split('=')[1]
        elif paramslist[i].split('=')[0].lower() == 'query':
            fieldValues[2] = paramslist[i].split('=')[1]
        elif paramslist[i].split('=')[0].lower() == 'items':
            fieldValues[3] = paramslist[i].split('=')[1]
        i += 1
    return fieldValues



def getScreenInput(fieldValues):

    fieldValues = easygui.multenterbox(msg = msg, title = title, fields = fieldNames, values = fieldValues )
        # make sure that none of the fields was left blank
    while 1:
        if fieldValues == None: break
        errmsg = ""
        for i in range(len(fieldNames)):
            if fieldValues[i].strip() == "":
                errmsg += ('"%s" is a required field.\n\n' % fieldNames[i])
            elif (i == 3) and  fieldValues[i].isdigit() is False:
                errmsg += ('"%s" has to be an integer.\n\n' % fieldNames[i])
        if errmsg == "":
            break # no problems found
        fieldValues = easygui.multenterbox(errmsg, title, fieldNames, fieldValues)

    return fieldValues

def printData(fieldValues):

    if fieldValues != None:


        api_key = fieldValues[0]
        api_secret = fieldValues[1]
        #access_token = fieldValues[2]
        #access_secret = fieldValues[3]
        query = fieldValues[2]

        if mode == Mode.PREVIEW:
            items = 5
        else:
            items = fieldValues[3]


        #printing all the tweets to the standard output
        auth = OAuthHandler(api_key, api_secret)
        #auth.set_access_token(access_token, access_secret)

        proxy_url = ""
        #test connectivity and set proxy
        test_con_url = "www.google.com" # For connection testing
        test_con_resouce = "/" # may change in future
        test_con = httplib.HTTPConnection(test_con_url) # create a connection

        try:
            test_con.request("GET", test_con_resouce) # do a GET request
            response = test_con.getresponse()
        except httplib.ResponseNotReady as e:
            easygui.msgbox("Improper connection state")
        except socket.gaierror as e:
            proxy_url = "proxy:8080"
        else:
            proxy_url = ""

        test_con.close()


        api = API(auth_handler = auth, proxy_url = proxy_url)

        print "beginDSInfo"
        print """fileName;#;true
    csv_first_row_has_column_names;true;true;
    csv_separator;|;true
    csv_number_grouping;,;true
    csv_number_decimal;.;true
    csv_date_format;d.M.yyyy;true"""
        print ''.join(['api_key;', fieldValues[0].encode('hex'), ';true'])
        print ''.join(['api_secret;', fieldValues[1].encode('hex'), ';true'])
        #print ''.join(['access_token;', fieldValues[2], ';true'])
        #print ''.join(['access_secret;', fieldValues[3], ';true'])
        print ''.join(['query;', fieldValues[2], ';true'])
        print ''.join(['items;', fieldValues[3], ';true'])
        print "endDSInfo"
        print "beginData"
        print 'User_Screen_Name, Geo, Profile_Img_Url, Source, Created_At, Text, Retweet_Count'
        try:
            for tweet in Cursor(api.search,
                                       q=query,
                                       count=100, #that's results per page
                                       result_type="recent",
                                       include_entities=True,
                                       lang="en").items(int(items)):
                tweet.text = tweet.text.replace("\r","")
                tweet.text = tweet.text.replace("\n","")

                print ''.join([tweet.user.screen_name.replace(',', ''), ', ', str(tweet.geo).replace(',', ''), ', ', \
                tweet.user.profile_image_url.replace(',', ''), ', ', tweet.source.replace(',', ''), ', ', \
                str(tweet.created_at), ', ', tweet.text.replace(',', ''), ', ',
                str(tweet.retweet_count), ''])

        except TweepError, e:
	        easygui.msgbox('failed because of %s' % e.reason)

        print "endData"




    else:
        print "beginDSInfo"
        print "endDSInfo"
        print "beginData"
        print """Error
User Cancelled"""
        print "endData"




if mode == Mode.PREVIEW:
    fieldValues = setArgs(fieldValues)
    #easygui.textbox(msg = 'preview1', text = sys.argv)
    fieldValues = getScreenInput(fieldValues)
    #easygui.textbox(msg = 'preview2', text = fieldValues)
    printData(fieldValues)
elif mode == Mode.EDIT:
    #easygui.textbox(msg = 'edit1', text = sys.argv)
    fieldValues = parseArgs(fieldValues)
    #easygui.textbox(msg = 'edit2', text = fieldValues)
    fieldValues = getScreenInput(fieldValues)
    #easygui.textbox(msg = 'edit2', text = fieldValues)
    printData(fieldValues)
elif mode == Mode.REFRESH:
    fieldValues = parseArgs(fieldValues)
    #easygui.textbox(msg = 'refresh1', text = sys.argv)
    printData(fieldValues)




