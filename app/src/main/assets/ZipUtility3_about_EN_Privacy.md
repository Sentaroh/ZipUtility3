### 1. Collected data
### 1.1. Data provided to ZipUtility3 by the user

The password for creating and decompressing the encrypted zip file is discarded when the app is closed and is never saved.

### 1.2.Activity record of ZipUtility3

When logging is enabled, the activity record data will be saved in the application's storage area for verification of the execution results of ZipUtility3 and for technical support.
<span style="color:red;"><u>Data will not be sent externally unless the "1.3. Sending or writing data outside of ZipUtility3" action is taken.</u></span>.

- Device information (manufacturer name, model name, OS version, mount point, app specific directory, StorageAccessFramework, Storage manager)
- ZipUtility3 version, ZipUtility3 execution options
- Directory name, file name, execution status
- Debug information
- Error information

### 1.3. Sending or writing data outside of ZipUtility3

The data held by ZipUtility3 cannot be sent or written out to the outside world unless operated by the user.

- Click the "Share button" from the History tab.
- Click the "Send to Developer" button from the System Information.
- Click the "Send to Developer" button from the Log Management page.
- Click "Export Log File" button from Log Management to export to external storage.

### 1.4.Deleting the data stored in ZipUtility3

By uninstalling ZipUtility3, the saved data ("1.2.ZipUtility3 Activity Log") will be deleted from the device.
<span style="color: red;"><u>However, data saved to external storage by user interaction will not be deleted. </u></span>

## 2.Permissions required to run the application.

### 2.1.Photos, media and files
**read the contents of your USB storage**.
**modify or delete the contents of your USB storage**.
Used for file synchronization and reading/writing management files.

### 2.2.Storage

### 2.2.1.Android 11 or later
**All file access**.

Used for file synchronization and reading/writing management files.

### 2.2.2.Android10 and earlier
**read the contents of your USB storage**.
**modify or delete the contents of your USB storage**.
Used for file synchronization and reading/writing management files.

### 2.3.Others

### 2.3.1.Prevent device from sleeping
Prevents devices from sleeping.
