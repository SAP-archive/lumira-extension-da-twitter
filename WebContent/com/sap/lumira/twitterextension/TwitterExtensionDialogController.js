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

define(function() {
    "use strict";

    var TwitterExtensionDialogController = function(acquisitionState, oDeferred, fServiceCall, workflow) {
    	
    	//Create dialog

        //button to close the dialog
        var closeDialogButton = new sap.m.Button({
            text: "Close",
            type: "Reject",
            width: "100px",
            press: function() {
                if (oDeferred.state() == "pending") {
                    this.destroy();
                    oDeferred.reject();
                }
                dialog.close();
            }
        });

        var dialog = new sap.m.Dialog({
            contentWidth: "720px",
            contentHeight: "480px",
            title: "Twitter Extension",
            endButton: closeDialogButton
        });

        /*
        Create dialog controls
        */
        
        var consumerKeyLabel = new sap.m.Label({
            text : "Consumer Key:",
            labelFor : consumerKeyText
        });
        
        dialog.addContent(consumerKeyLabel);

        var consumerKeyText = new sap.m.Input({
            width : '100%',
            value : "DaplbaJKKMTsxvUtTaoNOGxec",
        });
        
        dialog.addContent(consumerKeyText);
        
        var consumerSecretLabel = new sap.m.Label({
            text : "Consumer Secret:",
            labelFor : consumerSecretText
        });
        
        dialog.addContent(consumerSecretLabel);

        var consumerSecretText = new sap.m.Input({
            width : '100%',
            value : "NQSpKiXqG0T3OWdlKqCIeWeJGD9fTekqBtgQLqLP9dWhWZ2h9P"
        });
        
        dialog.addContent(consumerSecretText);
        
        var accessTokenLabel = new sap.m.Label({
            text : "Access Token:",
            labelFor : accessTokenText
        });
        
        dialog.addContent(accessTokenLabel);
        
        var accessTokenText = new sap.m.Input({
            width : '100%',
            value : "3528212894-ZrIfvAK5VKGO8W9Us0HS3wsbzVJPP0pNeERdxvQ"
        });
        
        dialog.addContent(accessTokenText);
        
        var accessSecretLabel = new sap.m.Label({
            text : "Access Secret:",
            labelFor : accessSecretText
        });
        
        dialog.addContent(accessSecretLabel);
        
        var accessSecretText = new sap.m.Input({
            width : '100%',
            value : "vjAQDBXBHB7nvh0MqO3uuwQXXWRXcwM2KNYxse837gZXW"
        });
              
        dialog.addContent(accessSecretText);

        
        /*
        Button press events
        */
        
        var okButtonPressed = function() {
            var info = {};
            info.consumerKey = consumerKeyText.getValue();
            info.consumerSecret = consumerSecretText.getValue();
            info.accessToken =  accessTokenText.getValue();
            info.accessSecret = accessSecretText.getValue();
            acquisitionState.info = JSON.stringify(info);
            oDeferred.resolve(acquisitionState, "Twitter Dataset");
            dialog.close();
        };

        var okButton = new sap.m.Button({
            text : "OK",
            type: "Accept",
            width: "100px",
            press: okButtonPressed
        }).addStyleClass("sample-da-button");
        
        dialog.addContent(okButton);

        /*
        Modify controls based on acquisitionState
        */
        var envProperties = acquisitionState.envProps;
        if (acquisitionState.info) {
            var info = JSON.parse(acquisitionState.info);
            consumerKeyText.setValue(info.consumerKey);
            consumerSecretText.setValue(info.consumerSecret);
            accessTokenText.setValue(info.accessToken);
            accessSecretText.setValue(info.accessSecret);
            envProperties.datasetName = info.consumerKey;
        }

        this.showDialog = function() {
            dialog.open();
            
          //adds custom css
           $(".sample-da-button").css("display", "block");
           $(".sample-da-button").css("margin-bottom", "2%");
        };
    };

    return TwitterExtensionDialogController;
});