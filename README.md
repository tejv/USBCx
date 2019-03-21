# USBCx - USB Power Delivery Protocol Analyzer
![alt text](https://github.com/tejv/USBCx/blob/master/gui_image.png)

--------------------------------------------------------------------------------
Quick Start Guide USBCx USBPD Protocol Analyzer
--------------------------------------------------------------------------------
Author: Tejender Sheoran

Email: tejendersheoran@gmail.com

Copyright (C) <2017>  <Tejender Sheoran>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--------------------------------------------------------------------------------

-------------------------------------------------------------------------------
Overview
--------------------------------------------------------------------------------
1. This analyzer uses CY4500 TypeC PD Anlyzer kit as low level hardware.
2. USBCx PD Analyzer GUI( USBCx-version.jar ) is used to present data to user. GUI
   is written in Java.
3. USB interface is used for data logging from hw to PC.
4. Tool works on both Windows and Linux. I have tested it on Windows 7/10 and Ubuntu.
5. This tool supports unlimited logging as it stores data periodically to harddisk.
6. Tool also supports detailed message parsing upto USBPD spec Rev3 v1.1.


--------------------------------------------------------------------------------
Setup
-------------------------------------------------------------------------------
1. Get the latest version (entire folder) from output folder.
2. Install latest JRE 8(Java Runtime environment). For building source use JDK 8.
3. Install Driver 
  ### Windows
    . Install libusb win32 driver. By default CY4500 kit will bind to cypress driver. 
      You need to manually reinstall libusb win32 driver.
    . Note if device is plugged to different port. You need to install libusb win32 driver
      for that port because by default device bind to cypress driver.
    . Easy way to do this is to use zadig tool from http://zadig.akeo.ie/
    . Download the tool and run it.
    . In Options menu click on List all devices. Then select the analyzer device and
      replace driver(not install WICD driver) to libusbk.
  ### Linux
    . Make sure user has read/write permisssion to the usb device. 
    . If not then Clicking Start/Stop menu item You will get " Start Command fail". To fix this create a file
      /etc/udev/rules.d/99-userusbdevices.rules
      
        with below lines and replug the device.
      SUBSYSTEM=="usb",ATTR{idVendor}=="04b4",ATTR{idProduct}=="0078",MODE="0660",GROUP="plugdev"
      SUBSYSTEM=="usb",ATTR{idVendor}=="04b4",ATTR{idProduct}=="b71d",MODE="0660",GROUP="plugdev"
      
        where 04b4 is the vendor id, 0078 is product id of usb device, b71d is product id of boot device.
        
4. Double click on USBCx-version.jar. In linux PC make sure to make this file executable.
5. Log window will show "USBCx HW Attached".
6. Boot the FW image USBCx-version.cyacd using download button(do not start capture before bootloading).
   This will put HW in boot mode and led will stop blinking. If download command failed then driver
   is not properly installed.
   
   Next automatically new window will open CyBootloaderHost application.
   Make sure libusb driver is bind
   for boot device in Windows PC( Boot mode use different PID hence libusb driver for main application won't work,
   use zadig tool again to replace driver to libusb if not so).
   
   Open cyacd file(in output folder)and click download. After download is over close bootload application.
   
   If CyBootloaderHost application does not open, then run it yourself. Its in BootloaderHost folder
   with name CyBootloaderHost-version.jar (in linux PC make it executable).
7. On main application, click version icon to check FW version match to that of cyacd file. 
8. On main application, Click start/stop button to start capturing. You will get success message in 
   status bar if correctly configured.
9. If CC1/CC2 voltages are not correct then HW (it need rev5 or greater) does not support this feature.
   Even if HW supports this feature, CC1/CC2 readings will saturate above 3.3V. 

--------------------------------------------------------------------------------
HW LED INDICATORS
--------------------------------------------------------------------------------
1. White - Analyzer is just idle with no start/stop. Default when connected to PC white led is glowing.
2. Green - Analyzer is capturing the data.
3. Yellow - Analyzer has stopped capturing data
4. Blue - Analyzer internal buffer has overflowed means data is not taken out from analyzer fast enough. 
   Once buffer overflow happens led keep glowing as blue even if buffer is emptied. This can be cleared by reset button.

--------------------------------------------------------------------------------
HW Triggers Description
--------------------------------------------------------------------------------
To enable Triggers. Go to Triggers tab.

1. Start Trigger(SOM): This pin triggers on start of a message. It will go high 
   then immediately go low at start of message. If check box for this trigger is selected
   and trigger is set using trigger set button, then trigger will be generated at start of
   message with specified "serial no" in adjacent textbox. If this trigger is not
   set then this trigger will be generated at start of every message.
   If you want to use this trigger. Then enter the Sno and Click Set Trigger button. 
   If everything is fine you will get a message "Trigger Set Successful". 
2. End Trigger(EOM): This pin triggers on end of a message. It will go high 
   then immediately go low at end of message. If check box for this trigger is selected
   and trigger is set using trigger set button, then trigger will be generated at end of
   message with specified "serial no" in adjacent textbox. If this trigger is not
   set then this trigger will be generated at end of every message.
   If you want to use this trigger. Then enter the Sno and Click Set Trigger button. 
   If everything is fine you will get a message "Trigger Set Successful"  
3. Msg  Trigger(MTR): This pin triggers on end of a particular message.
   This is enabled/disabled/Set from GUI. 
   You can enable/disable SOP type , message type, message id, message class, count to trigger on a unique 
   message. This feature is handy when you want to see waveforms on oscilloscope at 
   a particular event.   
   Click Set Trigger button. If everything is fine you will get a message "Trigger Set Successful".    

--------------------------------------------------------------------------------
Advanced options - Rp Monitoring
--------------------------------------------------------------------------------
The sniffer( HW Rev5 or greater) supports monitoring active cc channel for 
attach, detach, Rp change events. To enable this feature. 
1. Go to Advanced Options tab.
2. Check Rp monitoring feature and set debounce in ms. Default debounce is 15ms.
3. Select active cc channel radio button close to CC1/CC2 voltage readings on in top right corner of GUI.
   This can be done based on CC1/2 voltage readings or guess work.
4. Next click reset button.

After this analyzer will report all events.
Note: If you see "DETACH" event after "VBUS DN" that's because it is debouncing.
Actual detach happened before "VBUS DN". Just check delta field to see diff between
events.
 
--------------------------------------------------------------------------------
Advanced options - xScope
--------------------------------------------------------------------------------
1. xScope captures CC1/CC2/VBUS voltages and VBUS current at 1000 samples/second.
2. xScope will be populated when capture is stopped.
3. Click on a particular message will move xscope to window which contains that messsage.
   To know where message lies. Enable start time in packet window by clicking + sign in top right corner.
   Then start time can be related to graph x axis.
4. Use next/previous buttons in side pane to move through graphs.
5. Delta X, Delta Y are difference between last 2 mouse clicks on xScope canvas.
6. Mouse wheel will change resolution. 

--------------------------------------------------------------------------------
Advanced options - Terminations
--------------------------------------------------------------------------------
1. Terminations tab can be used to apply Rp/Rd/Ra on cc lines.

--------------------------------------------------------------------------------  
GUI Controls
--------------------------------------------------------------------------------

1. Start/Stop Capture- Enable/Disable the snooping logic. Reset timer and counters.
2. Reset - Clear the current data as well as reset the hw. (i.e. Stop + Start)
3. Download FW -  Updates the firmware. Use the .cyacd file to upgrade the fw.
4. Get HW/FW version- Displays current version of HW/FW.
6. Save - Saves the accumulated data to .uc file. 
7. Open - Open a .uc file and load it on display.
8. Drag and drop a .ucx1 file onto dataview will also open the file.
9. Terminations tab can be used to apply Rp/Rd/Ra on cc lines.

--------------------------------------------------------------------------------   
Working
--------------------------------------------------------------------------------
1. Click on Start/Stop Capture button to enable/disable analyzing. Status bar will display "Start/Stop Success"
2. Use Set trigger button to set triggers.  
   
--------------------------------------------------------------------------------   
Other Features
--------------------------------------------------------------------------------
1. Duration field show the total duration a packet in us.
2. Delta field show time difference between end of last packet and start of current packet in us.
3. Start Delta : show the start time difference of last 2 selected messages in us.
4. You can hide/show any column by clicking "+" sign at top right corner of table view.
5. You can rearrange columns as per your convenience by dragging column headers.
6. Drag and drop a .ucx1 file onto data view will also open the file.
7. If "OK" field is blue that means packet has EOP error.
8. If "OK" field is yellow means CRC error.
9. If "OK" field is red means both crc and eop error in packet.
