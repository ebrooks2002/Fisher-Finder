# Welcome to Fisher Finder!
## About:
Fisher Finder, developed for Android, is a lightweight mobile app to assist users in managing and locating their SPOT TRACE devices.

Why use Fisher Finder? First, SPOT’s mobile app does not display the user’s GPS location, meaning users cannot see their relative distance or heading to a TRACE device. Also, Fisher Finder allows device data to be kept for offline use. When a user leaves an area with cell coverage, the app continues functioning using last received TRACE device data.

## Getting Started:

1. Download the app-debug.apk from the releases section on the right. 

2. Open the file on your Android device.

3. If prompted, allow "Install from Unknown Sources" in your browser or file manager settings.

4. If a "Play Protect" warning appears, click "Install Without Scanning" or "Scan with Google"

Here's what the app should look like:

<p align="center">
  <img src="https://github.com/user-attachments/assets/dc013053-f377-4613-9dfc-ad758fe09fed" width="300" height="650" alt="Fisher Finder Screenshot">
  <img src="https://github.com/user-attachments/assets/7ae4dda4-f9da-4462-bfa9-d1dc58dfef10" width = "300" height="650" alt="Fisher Finder Screenshot">
</p>


*Note: Your phone must have a magnetometer and accelerometer for the compass feature to be fully functional.*

The top left button is for selecting which asset's (TRACE device) information you want to view. Selecting it will show you:
 - The asset's GPS coordinates
 - Date and time of most recent update
 - The asset's distance from Tema Harbour.
 - Your distance from the asset.
 - Asset's speed. Warning: speed is calculated by dividing distance between last two recorded locations by time between those updates. Therefore, this reading is a general approximation.

Below that you'll be shown Device Info:
- Course (Direction you're moving towards).
- Bearing (Direction asset is in).
- Heading (Direction your phone is pointed towards).
- A compass. There are two arrows, red and blue, representing course and heading, respectively. If the phone isn't moving, the course arrow will be colored grey.
The very top of the compass represents 0° North. Ex: If the user's course and heading are north, both arrows will be pointed up. On the circle surrounding the arrows, there will be a dot representing the selected asset. The position of the dot on the circle reflects the "Bearing" reading. Ex: If your selected asset lies 99 ° East of you, it will be on the right side of the circle, as shown in the screenshot above.

The map is centered on the coast of Ghana with bathymetry lines. Assets are projected on the map. Their color (green, yellow, or red) is based on how recently they've sent a satellite signal.

To use a custom map, go the the 'assets' folder and replace the .mbtiles file and .json file with your own. To view your own SPOT TRACEs, replace the XML Feed ID string in network/SPOTApiService.kt with your Feed ID.

If you find any bugs in the app, please create a new issue describing it.

## Who:

This project is being built and maintained by Ethan Brooks under the guidance of the Omand Lab at the University of Rhode Island Graduate School of Oceanography.
