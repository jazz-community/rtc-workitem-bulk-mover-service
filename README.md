# Work Item Bulk Mover Service
While RTC supports to move a single Work Item from one project area to another, it lacks the capability of doing so for a bunch of Work Items at once.

This project contains the RTC web service extension only. It does not provide a user interface. The user interface can be found within the [Bulk Mover Plug-in](https://github.com/jazz-community/rtc-workitem-bulk-mover-ui) project. It provides a user interface to intract with this service.

## Setup Instructions
The most convenient way to install this service is to download the current stable release from the [Releases](https://github.com/jazz-community/rtc-workitem-bulk-mover-service/releases) page.
It is highly recommended to then follow the instructions provided in the [Bulk Mover Plug-in](https://github.com/jazz-community/rtc-workitem-bulk-mover-ui) project.

## API Reference
This section contains the API definition for this project. While we try to do our best to keep this accurate, it is always best to have a look at the code in order to make sure that everything is covered.

### base path
You'll see the term `<root>` within the following API doc a few times. It represents the root (or base) path for this service. Assuming that your CCM server is being referred to as `localhost:7443/ccm`, the `<root>` path for this service is the following:

> https://localhost:7443/jazz/service/com.siemens.bt.jazz.services.WorkItemBulkMover.IWorkItemBulkMoverService

### get project area
List all available project areas.

> GET <root>/project-areas

*Example Response:*
```
[
   {
      "id":"[UUID _3Ud7oP5aEeanwOtOCiP3RQ]",
      "name":"SAFe Portfolio"
   },
   {
      "id":"[UUID _sFpSwP5aEeanwOtOCiP3RQ]",
      "name":"SAFe Program"
   }
]
```

### Start work item move
This will start the core Bulk Mover operation. 

> POST /move

*Request / Response* 
```
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
- Custom Attributes: The handling of custom attributes is limited to the features provided by IBM. For anything else than plain text, we cannot ensure proper handling of those.
- States: State mapping is currently not possible.

## Contributing
Please use the [Issue Tracker](https://github.com/jazz-community/rtc-workitem-bulk-mover-service/issues) of this repository to report issues or suggest enhancements.

For general contribution guidelines, please refer to [CONTRIBUTING.md](https://github.com/jazz-community/rtc-workitem-bulk-mover-service/blob/master/CONTRIBUTING.md)

## Licensing
Copyright (c) Siemens AG. All rights reserved.<br>
Licensed under the [MIT](LICENSE) License.
