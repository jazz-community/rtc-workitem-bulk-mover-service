# Work Item Bulk Mover Service
While RTC supports to move a single Work Item from one project area to another, it lacks the capability of doing so for a bunch of Work Items at once.

This project contains the RTC web service extension only. It is best to use the [Bulk Mover Plug-in](https://github.com/jazz-community/rtc-workitem-bulk-mover-ui) as it provides a user interface for the operations provided through this service.

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

*Request Body*
```
...
```

*Response*
```
...
```

## Limitations
- Custom Attributes: The handling of custom attributes is limited to the features provided by IBM. For anything else than plain text, we cannot ensure proper handling of those.
- States: State mapping is currently not possible

## Project Status
Be reminded that this service is still in development and should not be considered stable.

## Setup Instructions
Will follow soon...

## Contributing
Please use the [Issue Tracker](https://github.com/jazz-community/rtc-workitem-bulk-mover-service/issues) of this repository to report issues or suggest enhancements.<br>
Pull requests are very welcome.

## Licensing
Copyright (c) Siemens AG. All rights reserved.<br>
Licensed under the [MIT](LICENSE) License.
