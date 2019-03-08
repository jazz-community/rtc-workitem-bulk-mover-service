# Work Item Bulk Mover Service
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fjazz-community%2Frtc-workitem-bulk-mover-service.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fjazz-community%2Frtc-workitem-bulk-mover-service?ref=badge_shield)

While RTC supports to move a single Work Item from one project area to another, it lacks the capability of doing so for a bunch of Work Items at once.

This project contains the RTC web service extension only. It does not provide a user interface. The user interface can be found within the [Bulk Mover Plug-in](https://github.com/jazz-community/rtc-workitem-bulk-mover-ui) project. It provides a user interface to intract with this service.

## Setup Instructions
The most convenient way to install this service is to download the current stable release from the [Releases](https://github.com/jazz-community/rtc-workitem-bulk-mover-service/releases) page.
It is highly recommended to then follow the instructions provided in the [Bulk Mover Plug-in](https://github.com/jazz-community/rtc-workitem-bulk-mover-ui) project.

## API Reference
This section contains the API definition for this project. While we try to do our best to keep this accurate, it is always best to have a look at the code in order to make sure that everything is covered.

### Base Path
You'll see the term `BASE` within the following API doc a few times. It represents the root (or base) path for this service. Assuming that your CCM server is being referred to as `localhost:7443/ccm`, the `BASE` path for this service is the following:

> https://localhost:7443/jazz/service/com.siemens.bt.jazz.services.WorkItemBulkMover.IWorkItemBulkMoverService

### Get Service Infos
List the current service version.
> GET `BASE`/info

*Example Response:*
```javascript
{"version":"1.4.3.201809222050"}
```

### List Project Areas
List all available project areas a user has access to.
> GET `BASE`/project-areas

You can provide a black list of project areas that should not be included in the response (e.g. in case you want the current source area not to be an option for a move). To do that, append the project areas UUID as a parameter, seperate multiple items with a comma:
> GET `BASE`/project-areas?ignore=_3Ud7oP5aEeanwOtOCiP3RQ,_3Ud7oP5aEeanwOtOCiP3RQ

*Example Response:*
```javascript
[
   {
      "id":"_3Ud7oP5aEeanwOtOCiP3RQ",
      "name":"SAFe Portfolio"
   },
   {
      "id":"_sFpSwP5aEeanwOtOCiP3RQ",
      "name":"SAFe Program"
   }
]
```

### List Work Item Types
List all available work item types within a project area. The `project-area` parameter is required and you have to provide the display name of the project area in order to get its work item types.
> GET `BASE`/types?project-area="SAFe Program"

*Example Response:*
```javascript
[
   {
      "id":"defect",
      "name":"Defect"
   },
   {
      "id":"com.ibm.team.apt.workItemType.story",
      "name":"Story"
   }
]
```

### Start or Preview a Work Item Move
This will start the core Bulk Mover operation. You can either preview the mapping (setting `previewOnly` to `true`) or you can try to move all workitems directly. The server response will be in the same base structure as the client request. This allows a client application like the [Bulk Mover Plug-in](https://github.com/jazz-community/rtc-workitem-bulk-mover-ui) to reuse the response in the user interface and send it back again. This is especially useful when it comes to missing data that the user needs to enter.

> POST `BASE`/move

*Request / Response* 
```javascript
{
   // REQUEST only
   "previewOnly":false, // true if you want to preview the changes, but do not move yet. Setting to false or providing no value will move if possible
   "targetProjectArea":"My Target", // Display name of the target project area
   "workItems":[153,156,199], // the list of work item IDs that will be moved
   // RESPONSE only
   "error":"some error message returned by the server", 
   "successful":false, // Indicates whether move was successful
   // REQUEST and RESPONSE
   "mapping":[ // mapping definition for teh user to fill out
      {
         "identifier":"category", // internal attribute ID
         "displayName":"Filed Against", // attribute display name
         "allowedValues":[ // a list of allowed values for this attribute
            {
               "identifier":"/Unassigned/Infrastructure/",
               "displayName":"Infrastructure"
            },
	    ...
         ],
         "valueMappings":[ // a list of old values and the affected work items
            {
               "oldValue":{ // the value used for this attribute in the source project area
                  "identifier":"/Unassigned/Team 1 (rename)/",
                  "displayName":"Team 1 (rename)"
               },
               "affectedWorkItems":[
                  {
                     "workItem":{
                        "id":1446,
                        "title":"fix defect"
                     },
                     "chosen":"", // used by the client to set whether attribute is checked or not
                     "isRequired":true // is attribute reqired for this work item
                  },
                  ...
               ],
               "chosen":"", 
               "showDetails":false // used by the client to track if group is expanded or not
            }
         ]
      },
      ...
   ]
}
```

## Limitations
- Mapping required custom attributes only available in the target project area is currently not implemented for all attribute types. The most important and commonly used once are implemented however.
- Older releases of RTC may not be able to activate the service, see [this](https://github.com/jazz-community/rtc-workitem-bulk-mover-service/issues/11) discussion.

## Contributing
Please use the [Issue Tracker](https://github.com/jazz-community/rtc-workitem-bulk-mover-service/issues) of this repository to report issues or suggest enhancements.

For general contribution guidelines, please refer to [CONTRIBUTING.md](https://github.com/jazz-community/rtc-workitem-bulk-mover-service/blob/master/CONTRIBUTING.md)

## Licensing
Copyright (c) Siemens AG. All rights reserved.<br>
Licensed under the [MIT](LICENSE) License.


[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fjazz-community%2Frtc-workitem-bulk-mover-service.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fjazz-community%2Frtc-workitem-bulk-mover-service?ref=badge_large)