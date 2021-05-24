### 1.Collected data  
### 1.1.Data provided to ZipUtility3 by the user  

- Password for creating and extracting the encrypted ZIP file.  
The password will be discarded and not saved when the application is closed.  
- Password to protect data in "1.4. Send or export data outside ZipUtility3"   
The password will be discarded and not saved when the process is finished.  

### 1.2.ZipUtility3 Activity Log  

When logging is enabled, activity data will be stored in the app's memory area for verification of the app's execution results and for technical support. If logging is disabled, data recording will be stopped, however, data already recorded will not be deleted.  
<span style="color:red;"><u>Data will not be sent externally unless the "1.3. Sending or writing data outside of ZipUtility3" action is taken.</u></span>.  

- Device information (manufacturer name, model name, OS version, mount point, app specific directory, StorageAccessFramework, Storage manager)  
- ZipUtility3 version, ZipUtility3 execution options  
- Directory name, file name, execution status  
- Debug information  
- Error information  

### 1.3.Sending or writing data outside of ZipUtility3  

The data held by ZipUtility3 cannot be sent or written out to the outside world unless operated by the user.  

- Click the "Send to Developer" button from the System Information.  
- Click the "Share" button from the log management.  
- Click the "Send to Developer" button from the log management.  
If you specify a password, the attached file will be password protected.  
- Click "Exporting logs" button from Log Management to export to external storage.  

### 1.4.Deleting the data stored in ZipUtility3  

By uninstalling ZipUtility3, the saved data ("1.2.ZipUtility3 Activity Log") will be deleted from the device.  
<span style="color: red;"><u>However, data saved to external storage by user interaction will not be deleted. </u></span>  

## 2.Permissions required to run the application.  

### 2.1.Photos, Media and Files  
**<u>read the contents of your USB storage</u>**  
**<u>modify or delete the contents of your USB storage</u>**  
Used for file and directory operations (create, delete, rename), ZIP file operations (create, delete, rename, update, extract) and log file writing.  

### 2.2.Others  

### 2.2.1.Prevent device from sleeping  
Used to prevent the device from going to sleep during file/directory and ZIP file operations.  
